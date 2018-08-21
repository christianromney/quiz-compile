# Quiz Compiler

Compiles input files written in a simple, custom DSL to MP3 audio files.
Currently only works on Mac OS X. I use these files to power a physical
computing project based on Raspberry PI that plays the audio and collects
user input through push buttons (like a simple modern Speak 'N Spell).

## Dependencies

Install Clojure and Lame.

    $ brew install clojure lame


## Usage

    $ lein do clean, run [voice] [question bank]

## Options

* `[voice]` - the name of an installed Voice Over voice (defaults to Serena)
* `[question bank]` - the name (minus extension) of a file under
  **resources/input** to compile (defaults to electrical-engineering).

Run `say -v '?'` at the command line to see installed voices.
Tip: I've found slowing the Rate to 33 sounds better than the default.

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

## License

Copyright © 2018 Christian Romney

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
