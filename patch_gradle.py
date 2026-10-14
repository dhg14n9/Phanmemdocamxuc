import re

with open("app/build.gradle.kts", "r") as f:
    content = f.read()

replacement = r'''  testOptions { unitTests { isIncludeAndroidResources = true } }
  androidResources {
    noCompress += listOf("mdl", "conf", "fst", "int", "dubm", "ie", "mat", "stats", "uuid", "am", "graph", "ivector")
  }'''

content = content.replace("  testOptions { unitTests { isIncludeAndroidResources = true } }", replacement)

with open("app/build.gradle.kts", "w") as f:
    f.write(content)
