# glygenarray-java-client
There are several client interfaces that can be used for accessing different sets of web services
of glygen array repository. One example usage is shown below:

```
GlycanRestClient client = new GlycanRestClientImpl();
client.setUsername("xxxxx");
client.setPassword("yyyyy");
client.setURL("https://glygen.ccrc.uga.edu/ggarray/api/");
Glycan glycan = new SequenceDefinedGlycan();
glycan.setName("TestingClient");
// set other details such as sequence
System.out.println(client.addGlycan(glycan));
```

The API is in progress. More functionality to the java client is being added as the repository functionality progresses.
