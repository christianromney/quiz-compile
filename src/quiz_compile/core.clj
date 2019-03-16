(ns quiz-compile.core
  (:require [clojure.java.io :as io]
            [clojure.java.shell :refer [sh]]
            [clojure.pprint :refer [pprint]]
            [clojure.string :as str])
  (:gen-class))

;; -- ASCII --

(def ^:const A 65)
(def ^:const Z 90)
(def ^:const alphabet
  "A map of answer indeces to a corresponding letter"
  (reduce merge
          (map-indexed
           (fn [idx ascii]
             {idx (str (char ascii))}) (range A (inc Z)))))


(defn trace [x] (pprint x) x)

;; -- API --

(defn read-source
  "Reads an input file containing data in our question/answer DSL. Returns
  sequences of questions and answers."
  [file]
  (let [has-content? (complement str/blank?)]
    (with-open [rdr (io/reader file)]
      (filterv (comp has-content? first)
               (partition-by has-content? (line-seq rdr))))))

(defn parse
  "Parses a sequence of question and answer sequences into data structures
  describing the syntactic elements (an AST)."
  [questions]
  (letfn [(correct?
            [answer]
            (str/starts-with? answer "+"))

          (parse-answer
            [idx raw]
            {:choice (alphabet idx)
             :text (str/join " " (rest (str/split raw #"\s")))
             :correct? (correct? raw)})

          (parse-question
            [question]
            (let [[text & answers] question
                  parsed (map-indexed parse-answer answers)
                  correct (:choice (first (filter :correct? parsed)))]
              {:question text
               :answers parsed
               :correct-answer correct}))]
    (mapv parse-question questions)))

(defn compile-ast
  "Compiles the parsed questions and answers into the output format text.
  (two English sentences: the first containing the question and the second
  listing the multiple choice answers)."
  [ast]
  (letfn [(assemble-answer
            [answer]
            (format "%s) %s" (str/upper-case (:choice answer)) (:text answer)))

          (assemble-answers
            [data]
            (let [assembled (map assemble-answer (:answers data))]
              (format "%s, or %s"
                      (str/join ", " (butlast assembled))
                      (last assembled))))

          (assemble-question
            [data]
            {(:correct-answer data)
             [(str (:question data)
                   " Is it: "
                   (assemble-answers data)
                   "...")]})]
    (reduce (partial merge-with into) {}
            (mapv assemble-question ast))))

(defn emit
  "Emits the compiled sentences as files which can be converted to audio using Mac
  OS X's command-line Voice Over utility. Drives the text-to-speech and
  conversion to mp3 by shelling out to the system."
  [voice rate bank data]
  (letfn [(create-directories!
            [filename]
            (io/make-parents filename))

          (write-text-file!
            [filename data]
            (spit filename data))

          (write-intermediate-audio!
            [voice rate in-file out-file]
            (pprint {:voice voice :rate rate :input in-file :output out-file})
            (sh "say" "-v" voice "-r" rate "-f" in-file "-o" out-file))

          (write-compressed-audio!
            [in-file out-file]
            (sh "lame" "-m" "m" in-file out-file))

          (emit-files! [voice rate path-base data message]
            (let [path-text (str path-base ".out")
                  path-aiff (str path-base ".aiff")
                  path-mp3  (str path-base ".mp3")]

              (create-directories! path-text)
              (write-text-file! path-text data)
              (write-intermediate-audio! voice rate path-text path-aiff)
              (write-compressed-audio! path-aiff path-mp3)))]
    (let [file-base (format "target/%s/%s" bank "topic")]
      (emit-files! voice rate file-base
                   (str/replace bank  #"\W" " ")
                   (format "Compiling topic %s.mp3" file-base)))
    (doseq [[folder questions] data]
      (loop [qs questions idx 1]
        (if-not (seq qs)
          :done
          (let [file-base (format "target/%s/%s/q%s"
                                 (str/lower-case bank)
                                 (str/lower-case folder) idx)
                message  (format "Compiling %s.mp3" file-base)]
            (emit-files! voice rate file-base (first qs) message)
            (recur (rest qs) (inc idx))))))
    data))

;; -- runner --

(defn -main
  "The application entry point. Accepts three optional positional arguments:
  voice - the name of the Voice Over voice to use (default: Samantha)
  rate  - the rate at which the voice should speak (default: 30)
  topic - the named question bank file (minus the extension)."
  [& args]
  (let [args  (vec args)
        voice (get args 0 "Samantha")
        rate  (get args 1 "28")
        bank  (get args 2 "electrical-engineering")]
    (pprint {:args args :voice voice :rate rate :bank bank})
    (println (format "Compiling %s questions to spoken audio read by %s at rate %s ..." bank voice rate))

    (->> bank
         (format "input/%s.txt")
         io/resource
         read-source
         parse
         compile-ast
         (emit voice rate bank))

    (println "Done.")
    (shutdown-agents)))
