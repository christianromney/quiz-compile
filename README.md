# Quiz Compiler

Compiles input files written in a simple, custom DSL to MP3 audio files.
**Currently only works on Mac OS X.** I use these files to power a physical
computing project based on Raspberry PI that plays the audio and collects
user input through push buttons (like a simple modern Speak 'N Spell).

## Dependencies

Install Clojure, Leiningen, and Lame.

    $ brew install clojure leiningen lame

## Usage

    $ lein do clean, run [voice] [question bank]

Each question from the question bank will be compiled to an MP3 file in a
subfolder corresponding to the correct answer under the `target` directory.

## Options

* `[voice]` - the name of an installed Voice Over voice (defaults to Serena)
* `[question bank]` - the name (minus extension) of a file under
  **resources/input** to compile (defaults to electrical-engineering).

Run `say -v '?'` at the command line to see installed voices.

Tip: I've found slowing the speech rate to 33 in the Voice Over utility sounds
better than the default setting.

## DSL Example

The input source is a plain text file containing a question on one line,
followed by multiple answers. Each of the answers must begin with either a `+`,
which denotes the correct answer, or a `-` which denotes an incorrect answer.

Questions must be separated by one or more blank lines.

```
What is the standard unit of measure of current?
+ amps
- volts
- ohms

What kind of charge do electrons carry?
- no charge
+ negative charge
- positive charge

What physical property is measured in ohms?
- voltage
- current
+ resistance

```

[Source file](https://github.com/christianromney/quiz-compile/blob/master/resources/input/electrical-engineering.txt)

## Sample Output

[MP3](http://christianromney.org.s3.amazonaws.com/quiz/q1.mp3)

## License

Copyright © 2018 Christian Romney

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
