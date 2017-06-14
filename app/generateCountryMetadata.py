import yaml
import os
import sys
import shutil

sourceDir = sys.argv[1]
targetDir = sys.argv[2]
comment = sys.argv[3]

if os.path.exists(targetDir):
	shutil.rmtree(targetDir)
os.makedirs(targetDir)

for filename in os.listdir(sourceDir):
	print(filename)
	if filename.endswith(".yml"):
		basename = os.path.splitext(filename)[0]
		with open(sourceDir + filename, 'r') as file:
			data = yaml.load(file.read());
			for key, value in data.items():
				targetFileName = targetDir + key + ".yml"
				
				if os.path.isfile(targetFileName):
					targetFile = open(targetFileName, "a")
				else:
					targetFile = open(targetFileName, "w")
					targetFile.write("# "+comment+"\n")
				
				dump = yaml.safe_dump(value)
				if dump.endswith("\n...\n"):
					dump = dump[:-4]
				targetFile.write(basename + ": " + dump)
				
				targetFile.close()