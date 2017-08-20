#!/usr/bin/env python
# This script does not work, currently.

import argparse, subprocess, cmd
import re

exclude_pattern = '^(rect|layer|path|use|g\d|svg|text|tspan|outline|image|circle|ellipse)\d'
inkscape = "C:\Program Files\Inkscape\inkscape.com"

parser = argparse.ArgumentParser(description='''Export objects in SVG files into single files''')
parser.add_argument('infiles', nargs='+', help='SVG file(s) to export objects from, wildcards are supported')

args = parser.parse_args()

for infile in args.infiles:

    print('' + infile + ':')

    all_objects = subprocess.check_output([inkscape, "--query-all", infile])

    for obj in all_objects.splitlines():
        obj_id = obj.decode("utf-8").split(',')[0]
        match = re.search(exclude_pattern, obj_id)
        if (match == None):
            destfile = 'out/' + obj_id + '.svg'
            print(' '+obj_id+' to '+destfile)
            command = '"' + inkscape + '" --export-id "'+obj_id+'" --vacuum-defs --export-id-only --export-text-to-path --export-area-page --export-plain-svg "'+destfile+'" '+infile
            subprocess.check_output(command, shell=True)
