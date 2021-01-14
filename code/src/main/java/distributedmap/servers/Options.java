package distributedmap.servers;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;


public class Options {

    @Parameter(names = {"-h", "-?", "--help"}, help = true, description = "display usage information")
    public boolean help = false;

    @Parameter(names = {"-n", "--number"}, required = true, description = "number of the server (>= 0)")
    public int number;


    public static Options parse(String[] args) {
        Options options = new Options();

        JCommander parser = JCommander
                .newBuilder()
                .addObject(options)
                .build();

        parser.setProgramName(Server.class.getSimpleName());

        try {
            parser.parse(args);
        } catch (ParameterException e) {
            System.out.println(e.getMessage());
            parser.usage();
            return null;
        }

        if (options.help || options.number < 0) {
            parser.usage();
            return null;
        }

        return options;
    }
}
