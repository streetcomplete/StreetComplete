import yaml
import os
import sys
import shutil

sourceDir = "../res/country_metadata/"
targetDir = "src/main/assets/country_metadata/"
comment = "Do not edit. Source files are in /res/country_metadata"

if os.path.exists(targetDir):
	shutil.rmtree(targetDir)
os.makedirs(targetDir)

for filename in sorted(os.listdir(sourceDir)):
	print(filename)
	if filename.endswith(".yml"):
		basename = os.path.splitext(filename)[0]
		with open(sourceDir + filename, 'r', encoding='utf8') as file:
			data = yaml.load(file.read());
			for key, value in data.items():
				targetFileName = targetDir + key + ".yml"
				
				if os.path.isfile(targetFileName):
					targetFile = open(targetFileName, "a", encoding='utf8')
				else:
					targetFile = open(targetFileName, "w", encoding='utf8')
					targetFile.write("# "+comment+"\n")
				
				dump = yaml.safe_dump(value)
				if dump.endswith("\n...\n"):
					dump = dump[:-4]
				targetFile.write(basename + ": " + dump)
				
				targetFile.close()