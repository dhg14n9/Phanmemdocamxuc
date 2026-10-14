import re

with open("app/build.gradle.kts", "r") as f:
    content = f.read()

replacement = r'''  androidResources {
    noCompress += listOf("mdl", "conf", "fst", "int", "dubm", "ie", "mat", "stats", "uuid", "am", "graph", "ivector")
  }
  packaging {
    jniLibs.useLegacyPackaging = true
  }'''

content = content.replace('''  androidResources {
    noCompress += listOf("mdl", "conf", "fst", "int", "dubm", "ie", "mat", "stats", "uuid", "am", "graph", "ivector")
  }''', replacement)

with open("app/build.gradle.kts", "w") as f:
    f.write(content)
