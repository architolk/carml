package nl.architolk.carml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import io.carml.model.TriplesMap;
import io.carml.util.RmlMappingLoader;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import io.carml.engine.rdf.RdfRmlMapper;
import java.nio.file.Paths;
import java.text.Normalizer.Form;
import org.eclipse.rdf4j.model.Model;
import io.carml.vocab.Rdf;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//import com.taxonic.carml.logical_source_resolver.CsvResolver;
import io.carml.logicalsourceresolver.JsonPathResolver;
//import com.taxonic.carml.logical_source_resolver.XPathResolver;

@SpringBootApplication
public class Convert {

  private static final Logger LOG = LoggerFactory.getLogger(Convert.class);

  public static void main(String[] args) {

    SpringApplication.run(Convert.class, args);

    if ((args.length == 3) || (args.length == 4)) {

      LOG.info("Starting conversion");
      LOG.info("Mapper file: {}",args[0]);
      LOG.info("Data directory: {}",args[1]);
      if (args.length==4) {
        LOG.info("Data file: {}", args[3]);
      }
      LOG.info("Output file: {}",args[2]);

      try {

        Set<TriplesMap> mapping =
          RmlMappingLoader
            .build()
            .load(RDFFormat.TURTLE, Paths.get(args[0]));

        RdfRmlMapper mapper =
          RdfRmlMapper
            .builder()
            .triplesMaps(mapping)
            // Add the resolvers to suit your need
            .setLogicalSourceResolver(Rdf.Ql.JsonPath, JsonPathResolver::getInstance)
            //.setLogicalSourceResolver(Rdf.Ql.XPath, new XPathResolver())
            //.setLogicalSourceResolver(Rdf.Ql.Csv, new CsvResolver())

            //-- optional: --
              // specify IRI unicode normalization form (default = NFC)
              // see http://www.unicode.org/unicode/reports/tr15/tr15-23.html
            .iriUnicodeNormalization(Form.NFKC)
              // set file directory for sources in mapping
            .fileResolver(Paths.get(args[1]))
              // set classpath basepath for sources in mapping
            //.classPathResolver("some/path")
              // specify casing of hex numbers in IRI percent encoding (default = true)
              // added for backwards compatibility with IRI encoding up until v0.2.3
            .iriUpperCasePercentEncoding(false)
            //---------------

            .build();

        /*
        if (args.length==4) {
          FileInputStream inputStream = new FileInputStream(args[3]);
          mapper.bindInputStream(inputStream);
        }
        */
        Model result = mapper.mapToModel();

        FileOutputStream outputStream = new FileOutputStream(args[2]);
        try {
          Rio.write(result, outputStream, RDFFormat.TURTLE);
        }
        finally {
          outputStream.close();
        }
        LOG.info("Done!");
      }
      catch (Exception e) {
        LOG.error(e.getMessage(),e);
      }
    } else {
      LOG.info("Usage: carml <mapper.rml.ttl> <input directory> <output.ttl> {<input file>}");
    }
  }
}
