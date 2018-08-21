(ns quiz-compile.core
  (:require [clojure.java.io :as io]
            [clojure.java.shell :refer [sh]]
            [clojure.string :as str])
  (:gen-class))

;; -- ASCII --

(def ^:private A 65)
(def ^:private Z 90)
(def ^:private alphabet
  "A map of answer indeces to a corresponding letter"
  (reduce merge
          (map-indexed
           (fn [idx ascii]
             {idx (str (char ascii))}) (range A (inc Z)))))

;; -- API --

(defn read-source
  "Reads an input resource file containing data in our question/answer DSL from
  the input resource folder. Returns sequences of questions and answers."
  [bank]
  (let [has-content? (complement str/blank?)]
    (with-open [rdr (io/reader (io/resource (format "input/%s.txt" bank)))]
      (let [data (partition-by has-content? (line-seq rdr))]
        (filterv (comp has-content? first) data)))))

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
            (format "\"%s\", %s"
                    (:choice answer)
                    (:text answer)))
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
                   "?")]})]

    (reduce (partial merge-with conj) {}
            (mapv assemble-question ast))))

(defn emit
  "Emits the compiled sentences as files which can be converted to audio using Mac
  OS X's command-line Voice Over utility. Drives the text-to-speech and
  conversion to mp3 by shelling out to the system."
  [voice bank data]
  (letfn [(create-directories!
            [filename]
            (io/make-parents filename))

          (write-text-file!
            [filename data]
            (spit filename data))

          (write-intermediate-audio!
            [voice in-file out-file]
            (sh "say" "-v" voice "-f" in-file "-o" out-file))

          (write-compressed-audio!
            [in-file out-file]
            (sh "lame" "-m" "m" in-file out-file))]
    (doseq [[folder questions] data]
      (loop [qs questions idx 1]
        (if-not (seq qs)
          :done
          (let [file-base (format "target/%s/%s/q%s"
                                  (str/lower-case bank)
                                  (str/lower-case folder)
                                  idx)
                text-file (str file-base ".out")
                aiff-file (str file-base ".aiff")
                mp3-file  (str file-base ".mp3")]
            (println "Compiling" mp3-file)
            (create-directories! text-file)
            (write-text-file! text-file (first qs))
            (write-intermediate-audio! voice text-file aiff-file)
            (write-compressed-audio! aiff-file mp3-file)
            (recur (rest qs) (inc idx))))))
    data))

;; -- runner --

(defn -main
  "The application entry point. Accepts two optional positional arguments:
  the name of the Voice Over voice to use and the named question bank file
  (minus the extension)."
  [& args]
  (let [voice (or (first args) "Serena")
        bank  (or (second args) "electrical-engineering")]
    (println (format "Compiling %s questions to spoken audio read by %s..." bank voice))

    (->> bank
         read-source
         parse
         compile-ast
         (emit voice bank))

    (println "Done.")
    (shutdown-agents)))
