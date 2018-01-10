# About

This program models the training of a Self-Organising Map (SOM) and prints out a graph of its convergence. Developed as part of a coursework assignment for Warwick's CS331 (Neural Computing) module.

# Program Requirements

The program is written in Clojure. In order to run it you will need to install [Leiningen](leiningen.org), a dependency management tool (which in turn requires a JRE).

Once you've installed Leiningen you can open an interpreter (REPL) using the command `lein repl`. You can compile the program into a standalone executable using `lein uberjar`, and you can run the program without compiling using `lein run`, followed by the arguments required (see Usage section below).

# Usage

Usage below is given assuming you have compiled the software with `lein uberjar`. If you have not, replace `java -jar coursework.jar` with `lein run` in all following commands.

`java -jar coursework.jar <size> <quant> <iters> [--visuals | --repl-mode | --schedule <schedule> <modifier>]`

Where the command-line arguments are as follows:

Mandatory arguments:
- size: The size of the SOM. For example, using "3" will generate a 3x3 SOM. Due to the convergence measure it is recommended that this value is odd.
- quant: The number of SOMs to train. Their convergence values will be combined when the convergence graph is drawn.
- iters: The number of iterations to perform on each SOM.

Optional arguments:
- --visuals: When passed to the program, this enables the visual display, which allows for the viewing of the SOM in real-time as it trains. EPILEPSY WARNING - THE DISPLAY FLICKERS VERY FAST.
- --schedule: When passed to the program, this allows modification of the annealing schedule. The schedule and modifier parameters **must** be passed to the program immediately afterwards in order for this to work. The schedule parameter is a string that identifies the type of annealing schedule, and the modifier paramter identifies the specific value to be used in that schedule. Acceptable values of schedule are:
    - default: The default schedule provided in the coursework specification. This is equivalent to simply not using the --schedule option.  *Note: a modifier value must still be provided, but it will be ignored*.
    - var-base: The schedule uses a variable base for lambda.
    - var-rat: The schedule uses a different ratio between lambda and gamma.
    - log-rat: The schedule uses a logathmic ratio between lambda and gamma, with base `modifier`.
- --repl-mode: When passed to the program, this disables the output of a convergence graph. (This is useful primarly when accessing program functionality from the REPL, as it instead returns a sequence containing the convergence values).

## Usage Examples

`java -jar coursework.jar 3 1 100000` trains one 3x3 SOM over 100,000 iterations. When finished, it displays a graph of the convergence value over time.

`java -jar coursework.jar 5 10 1000 --schedule var-base 2 --visuals` trains 10 5x5 SOMs over 1000 iterations using a base of 2 to calculate lambda, and displays an ongoing visual of the training. When finished, it displays a graph of the convergence value over time.

`java -jar coursework.jar 7 5 10000 --schedule log-rat 5 --repl-mode` trains 5 7x7 SOMs over 10,000 iterations using a logarithmic ratio between lambda and gamma with 5 as the base. (Note in this case as repl-mode is on and the visuals are not on, this will not output anything).
