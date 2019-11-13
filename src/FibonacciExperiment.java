import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.Random;

public class FibonacciExperiment {
    static ThreadMXBean bean = ManagementFactory.getThreadMXBean( );


    /* define constants */
    static int MAXDIGITSIZE = 10;
    static int numberOfTrials = 30;
    static int MAXINPUTSIZE  = 300;
    static int MININPUTSIZE  =  0;
    static String ResultsFolderPath = "/home/codyschroeder/Results/"; // pathname to results folder
    static FileWriter resultsFile;
    static PrintWriter resultsWriter;

    public static void main(String[] args) {
        // run the whole experiment at least twice, and expect to throw away the data from the earlier runs, before java has fully optimized
        System.out.println("Running first full experiment...");
        runFullExperiment("FibFormulaBigN-Exp1-ThrowAway.txt");
        System.out.println("Running second full experiment...");
        runFullExperiment("FibFormulaBigN-Exp2.txt");
        System.out.println("Running third full experiment...");
        runFullExperiment("FibFormulaBigN-Exp3.txt");
    }

    static void runFullExperiment(String resultsFileName) {

        BigInteger answer = null;
        long answer1 = 0;
        long x = 0;
        try {
            resultsFile = new FileWriter(ResultsFolderPath + resultsFileName);
            resultsWriter = new PrintWriter(resultsFile);

        } catch (Exception e) {
            System.out.println("*****!!!!!  Had a problem opening the results file " + ResultsFolderPath + resultsFileName);
            return; // not very foolproof... but we do expect to be able to create/open the file...
        }

        ThreadCpuStopWatch BatchStopwatch = new ThreadCpuStopWatch(); // for timing an entire set of trials
        ThreadCpuStopWatch TrialStopwatch = new ThreadCpuStopWatch(); // for timing an individual trial

        resultsWriter.println("#    DigitSize      T(avg runtime)    Doubling Ratio"); // # marks a comment in gnuplot data
        resultsWriter.flush();

        double previousTime = -1;
        double doublingRatio = 0;
        /* for each size of input we want to test: in this case starting small and doubling the size each time */
        for (int inputSize = 1; inputSize <= MAXDIGITSIZE; inputSize++) {
            // progress message...
            System.out.println("Running test for input size " + inputSize + " ... ");
            /* repeat for desired number of trials (for a specific size of input)... */
            long batchElapsedTime = 0;
            System.out.print("    Running trial batch...");
            /* force garbage collection before each batch of trials run so it is not included in the time */
            System.gc();
            // instead of timing each individual trial, we will time the entire set of trials (for a given input size)
            // and divide by the number of trials -- this reduces the impact of the amount of time it takes to call the
            // stopwatch methods themselves
            //BatchStopwatch.start(); // comment this line if timing trials individually

            // run the tirals
            for (long trial = 0; trial < numberOfTrials; trial++) {

                /* force garbage collection before each trial run so it is not included in the time */
                System.gc();
                x = digitSize(inputSize);

                TrialStopwatch.start(); // *** uncomment this line if timing trials individually
                /* run the function we're testing on the trial input */
                //answer = FibLoopBig(x);
                //answer = FibMatrixBig(inputSize);
                //answer1 = FibFormula(inputSize);
                answer = FibFormulaBig(inputSize);


                batchElapsedTime = batchElapsedTime + TrialStopwatch.elapsedTime(); // *** uncomment this line if timing trials individually
            }

            //batchElapsedTime = BatchStopwatch.elapsedTime(); // *** comment this line if timing trials individually
            double averageTimePerTrialInBatch = (double) batchElapsedTime / (double) numberOfTrials; // calculate the average time per trial in this batch

            //calculate doubling ratio
            doublingRatio = averageTimePerTrialInBatch / previousTime;
            previousTime = averageTimePerTrialInBatch;

            /* print data for this size of input */
            resultsWriter.printf("%12d  %15.2f  %14f\n", inputSize, averageTimePerTrialInBatch, doublingRatio); // might as well make the columns look nice
            resultsWriter.flush();
            System.out.println(" ....done.");

        }


    }

    //return a random num of digit size
    public static long digitSize(int size){
        double minvalue = Math.pow(10,size-1);
        double maxvalue = Math.pow(10,size)-1;
        return (long)(minvalue + Math.random()*(maxvalue-minvalue));
    }

    public static BigInteger FibLoopBig(long x){

        //if x is less than 2, return 1 as answer
        if(x < 2)
            return BigInteger.ONE;
        else{
            //initialize variables
            BigInteger secondToLast = BigInteger.ONE;
            BigInteger last = BigInteger.ONE;
            BigInteger current = BigInteger.ZERO;
            //loop summing last two numbers until at x, and swapping a with b and b with c
            for(int i = 2; i <= x; i++){
                current = secondToLast.add(last);
                secondToLast = last;
                last = current;
            }
            //return answer
            return current;

        }
    }

    //wrapper function
    public static BigInteger FibMatrixBig(long x){

        //declare result variable
        BigInteger result;

        //initialize  matrices
        BigInteger originalMatrix[][] = {{BigInteger.ONE, BigInteger.ONE},
                {BigInteger.ONE, BigInteger.ZERO}};
        BigInteger lastMatrix[][] = {{BigInteger.ONE, BigInteger.ZERO},
                {BigInteger.ZERO, BigInteger.ONE}};

        //call helper function
        MatrixPower(originalMatrix, lastMatrix, x);

        //find and return result
        result = lastMatrix[0][0];
        return result;
    }

    //use matrix multiplication to find result function
    static void MatrixPower(BigInteger matrix[][], BigInteger resultMatrix[][], long x){

        //declare temp variable
        long temp;

        //create temp matrix
        BigInteger tempMatrix[][] = {{matrix[0][0], matrix[0][1]},
                {matrix[1][0], matrix[1][1]}};

        //declare variables to hold values
        BigInteger topLeft, topRight, bottomLeft, bottomRight;

        for(temp = x; temp > 0; temp = temp/2){

            //check if leftmost bit is 1
            if(temp % 2 == 1){
                //multiply matrices
                topLeft = resultMatrix[0][0].multiply(tempMatrix[0][0]).add(resultMatrix[0][1].multiply(tempMatrix[1][0]));
                topRight = resultMatrix[0][0].multiply(tempMatrix[0][1]).add(resultMatrix[0][1].multiply(tempMatrix[1][1]));
                bottomLeft = resultMatrix[1][0].multiply(tempMatrix[0][0]).add(resultMatrix[1][1].multiply(tempMatrix[1][0]));
                bottomRight = resultMatrix[1][0].multiply(tempMatrix[0][1]).add(resultMatrix[1][1].multiply(tempMatrix[1][1]));
                //set values
                resultMatrix[0][0] = topLeft;
                resultMatrix[0][1] = topRight;
                resultMatrix[1][0] = bottomLeft;
                resultMatrix[1][1] = bottomRight;
            }

            //square temp matrix
            topLeft = tempMatrix[0][0].multiply(tempMatrix[0][0]).add(tempMatrix[0][1].multiply(tempMatrix[1][0]));
            topRight = tempMatrix[0][0].multiply(tempMatrix[0][1]).add(tempMatrix[0][1].multiply(tempMatrix[1][1]));
            bottomLeft = tempMatrix[1][0].multiply(tempMatrix[0][0]).add(tempMatrix[1][1].multiply(tempMatrix[1][0]));
            bottomRight = tempMatrix[1][0].multiply(tempMatrix[0][1]).add(tempMatrix[1][1].multiply(tempMatrix[1][1]));
            //set values
            tempMatrix[0][0] = topLeft;
            tempMatrix[0][1] = topRight;
            tempMatrix[1][0] = bottomLeft;
            tempMatrix[1][1] = bottomRight;
        }

    }

    //formula from here: http://www.maths.surrey.ac.uk/hosted-sites/R.Knott/Fibonacci/fibFormula.html
    public static long FibFormula(long x){
        //calculate phi and its negative using doubles
        double phi = (Math.sqrt(5) + 1) / 2;
        double negPhi = (1 - Math.sqrt(5)) / 2;
        //use formula for answer
        double answer = ((Math.pow(phi, x) - Math.pow(negPhi, x)) / Math.sqrt(5));
        //return by rounding
        return Math.round(answer);
    }

    //got help from here: https://www.tutorialspoint.com/java/math/java_math_bigdecimal.htm
    public static BigInteger FibFormulaBig(long x){

        BigInteger answer;
        //find number of digits to help calculate precision, use approx golden ratio
        int numOfDigits = (int) Math.ceil((x * Math.log10(1.6180339887498948482045868343656)) - ((Math.log10(5)) / 2));
        //set context for precision
        MathContext mc = new MathContext((int) numOfDigits + 4);
        //ensure digits is at least 1, will always be
        if(numOfDigits == 0)
            numOfDigits = 1;
        //create original variables
        BigDecimal one = new BigDecimal(1);
        BigDecimal two =  new BigDecimal(2);
        BigDecimal five = new BigDecimal(5);
        BigDecimal root = five.sqrt(mc);
        BigDecimal phi = (one.add(root)).divide(two);
        BigDecimal negPhi = (one.subtract(root).divide(two));
        //perform operations on variables
        BigDecimal first = phi.pow((int) x);
        BigDecimal second = negPhi.pow((int) x);
        BigDecimal third = first.subtract(second);
        BigDecimal four = third.divide(root, mc);

        answer = four.toBigInteger();

        return answer;

    }
}
