# Blurry

Finds the sharpest picture in a folder, automatically.

## Why Blurry?

I take tons of pictures with my iPhone. Many are duplicates, that I shot when light conditions are no good and I want more chances to have a sharp one. 
So at the end of the day I find myself losing hours trying to figure out what is the sharpest picture in a group of similar blurry ones. 
But I'm a developer, I can stop this shit.

## How it works

Blurry is a simple command-line utility that helps you finding the sharpest pictures in a folder.

Give to Blurry a folder full of pictures and it will analyze them all with a technique similar to the AutoFocus of modern cameras.

For each picture Blurry computes a sharpness score and adds it to the picture's filename. 
It's pretty fast, because it can guess the overall sharpness looking at a small sample of pixels, and uses all the cores of your CPU for the computation.

You can tell Blurry to restore original filenames running it again with an additional parameter.

## Getting started

1. Ensure to have [Java 8](https://java.com/download) installed on your system.
2. Download the [latest release](https://github.com/giusilvano/blurry/releases/download/v1.0.0/blurry.zip) of Blurry.
3. Extract the Blurry folder somewhere on your PC/Mac/whatever.
4. Run <code>blurry.bat</code> on Windows or <code>blurry.sh<code> on Linux/Unix/Mac.
5. Enjoy!
