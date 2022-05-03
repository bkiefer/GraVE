# PURPOSE
Convert the SceneMaker XML format (old and new) into a more concise and easier
to read XML format that can be digested by GraVE. The converted format removes
all redundancy in the input and uses relative instead of absolute coordinates,
which makes life much easier, especially for zooming etc.

- Build with maven --> target/SCMConvert.jar
  - cd to dir with xml files
  - java -jar <path>/SCMConvert.jar -v
- The -v flag converts into new VOnDA format
- Works also for older versions (like from Aliz-E and before)
