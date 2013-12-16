rajmipmap
=========

A simple java RGBA gamma-correct mipmap creator.  Reads in a level 0
RGBA png file, averages the components in a gammma-correct manner and
outputs RGBA PNG mipmaps for levels [1,n].  Gamma is currently
hard-coded to 2.2, but that is simple enough to change below.  Output
filenames are suffixed with "_1", "_2", etc.  Handles the non-square
image case and should also handle non-pow-2

## Usage

```bash
> javac rajmipmap.java
> java rajmipmap granite.png
make_mipmaps: granite.png
creating granite_1.png width=128 height=128
creating granite_2.png width=64 height=64
creating granite_3.png width=32 height=32
creating granite_4.png width=16 height=16
creating granite_5.png width=8 height=8
creating granite_6.png width=4 height=4
creating granite_7.png width=2 height=2
creating granite_8.png width=1 height=1
```

LICENSE
=======

Copyright (c) 2011 Roger Allen

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or (at
your option) any later version.

This program is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

See <http://www.gnu.org/licenses/>.
