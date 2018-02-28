# Hard Drop

A Java implementation of Tetris with a genetic AI.

## Generic Algorithm

The AI learns over time through a series of *generations*. Each member of a
generation's population plays a game of Tetris with a set of scoring weights.
For each piece playable in the game, the board is re-scored considering the
all valid placements and rotations of the piece. The highest score is taken
and the game continues. The score of the board is determined by summing each
of the following properties multiplied by its associated weight.

| Name           | Description |
| -------------- | ----------- |
| Sum Height     | The sum of each column's height. |
| Max Height     | The height of the tallest column. |
| Height Delta   | The difference between teh tallest and shortest column. |
| Height Average | The average of each column's height. |
| Holes          | The number of empty spaces below the top of a column. |
| Wells          | The height delta for columns which are two or more blocks lower than both of its neighbors. |
| Blockades      | The number of blocks placed directly over a hole. |
| Clears         | How many clears occur from placing the block (up to four). |

The first generation begins with random weights. After all of the games for a
generation have completed, the weights for the next generation's population is
determined. The *elite* population (the 25% highest scoring members) of the
previous generation are moved as-is into the next generation. The remaining
candidates are constructed by randomly pairing two candidates from the upper
50% of the previous population and choosing each weight randomly from one or
the other parent. With a 10% chance, the offspring will have a random *mutation*
and get a completely new weight.

This process continues indefinitely.

## License

Copyright (c) 2012 Eric Fritz

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
