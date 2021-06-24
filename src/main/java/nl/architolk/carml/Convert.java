package nl.architolk.carml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import com.taxonic.carml.model.TriplesMap;
import com.taxonic.carml.util.RmlMappingLoader;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import com.taxonic.carml.engine.RmlMapper;
import java.nio.file.Paths;
import java.text.Normalizer.Form;
import org.eclipse.rdf4j.model.Model;
import com.taxonic.carml.vocab.Rdf;
import java.io.FileOutputStream;

//import com.taxonic.carml.logical_source_resolver.CsvResolver;
import com.taxonic.carml.logical_source_resolver.JsonPathResolver;
//import com.taxonic.carml.logical_source_resolver.XPathResolver;

public class Convert {

  private static final Logger LOG = LoggerFactory.getLogger(Convert.class);

  public static void main(String[] args) {

    if (args.length == 3) {

      LOG.info("Starting conversion");
      LOG.info("Mapper file: {}",args[0]);
      LOG.info("Data directory: {}",args[1]);
      LOG.info("Output file: {}",args[2]);

      try {

        Set<TriplesMap> mapping =
          RmlMappingLoader
            .build()
            .load(RDFFormat.TURTLE, Paths.get(args[0]));

        RmlMapper mapper =
          RmlMapper
            .newBuilder()
            // Add the resolvers to suit your need
            .setLogicalSourceResolver(Rdf.Ql.JsonPath, new JsonPathResolver())
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

        Model result = mapper.map(mapping);

        FileOutputStream out = new FileOutputStream(args[2]);
        try {
          Rio.write(result, out, RDFFormat.TURTLE);
        }
        finally {
          out.close();
        }
        LOG.info("Done!");
      }
      catch (Exception e) {
        LOG.error(e.getMessage(),e);
      }
    } else {
      LOG.info("Usage: carml <mapper.rml.ttl> <input directory> <output.ttl");
    }
  }
}
