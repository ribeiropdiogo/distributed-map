package distributedmap.benchmark;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;


public class Options {

    @Parameter(names = {"-h", "-?", "--help"}, help = true, description = "display usage information")
    public boolean help = false;

    @Parameter(names = {"-t", "--time"}, description = "execution time in seconds (> 0)")
    public long execution_time = 30L;

    @Parameter(names = {"-u", "--users"}, description = "number of concurrent users (> 0)")
    public int concurrent_users = 10;


    public static Options parse(String[] args) {
        Options options = new Options();

        JCommander parser = JCommander
                .newBuilder()
                .addObject(options)
                .build();

        parser.setProgramName(Benchmark.class.getSimpleName());

        try {
            parser.parse(args);
        } catch (ParameterException e) {
            System.out.println(e.getMessage());
            parser.usage();
            return null;
        }

        if (options.help || options.execution_time <= 0) {
            parser.usage();
            return null;
        }

        return options;
    }
}
