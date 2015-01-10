# Blurry

Finds the sharpest picture in a folder, automatically.

## Why Blurry?

I take tons of pictures with my iPhone. Many are duplicates, that I shot when light conditions are no good and I want more chances to have a sharp one. 
So at the end of the day I find myself losing hours trying to figure out what is the sharpest picture in a group of similar blurry ones. 
But I'm a developer, I can stop this shit.

## How it works

Blurry is a simple command-line utility that helps you finding the sharpest pictures in a folder.

Give to Blurry a folder full of pictures, it will analyze each one of them using algorithms similar to the AutoFocus systems of modern cameras.

For each picture Blurry will compute a sharpness score, adding it to the picture's filename. 
It's pretty fast, because it can guess the overall sharpness looking at a small sample of pixels, and uses all the cores of your CPU for the computation.

You can tell Blurry to restore original filenames running it again with an additional parameter.

## Getting started

Put the binary somewhere and execute it from the shell to learn the basic commands. It's super simple, KISS.
