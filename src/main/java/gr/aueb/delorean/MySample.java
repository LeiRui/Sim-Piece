package gr.aueb.delorean;

import gr.aueb.delorean.simpiece.SimPiece;
import gr.aueb.delorean.simpiece.SimPieceSegment;
import gr.aueb.delorean.util.TimeSeries;
import gr.aueb.delorean.util.TimeSeriesReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.List;

public class MySample {

  // java -jar MySample-jar-with-dependencies.jar "D:\\kdd\\lts-exp\\datasets\\%s.csv" "D:\\kdd\\lts-exp\\notebook\\SimPieceResults\\%s-%s-%s.csv"
  public static void main(String[] args) {
    String filename = args[0]; // e.g., "D:\\kdd\\lts-exp\\datasets\\%s.csv";
    String csvFile = args[1]; // e.g., "D:\\kdd\\lts-exp\\notebook\\SimPieceResults\\%s-%s-%s.csv";

    String[] datasetNameList = new String[]{"HouseTwenty_TEST", "Lightning7_TEST", "Mallat_TEST",
        "Wine_TEST"};
    int[] noutList = new int[]{320, 360, 400, 440, 480, 520, 560, 600, 640};
    for (String datasetName : datasetNameList) {
      for (int nout : noutList) {
        // apply Sim-Piece on the input file, outputting nout points saved in csvFile
        boolean hasHeader = false;
        int N = -1;
        int start = 0;
        try (FileInputStream inputStream = new FileInputStream(
            String.format(filename, datasetName))) {
          String delimiter = ",";
          TimeSeries ts = TimeSeriesReader.getMyTimeSeries(inputStream, delimiter, false, N,
              start, hasHeader);
          double epsilon = getSimPieceParam(nout, ts, 1e-6);
          System.out.println(datasetName + ": n=" + N + ",m=" + nout + ",epsilon=" + epsilon);
          SimPiece simPiece = new SimPiece(ts.data, epsilon);
          List<SimPieceSegment> segments = simPiece.segments;
          segments.sort(Comparator.comparingLong(SimPieceSegment::getInitTimestamp));
          try (PrintWriter writer = new PrintWriter(
              new FileWriter(String.format(csvFile, datasetName, N, nout)))) {
            for (int i = 0; i < segments.size() - 1; i++) {
              // start point of this segment
              writer.println(segments.get(i).getInitTimestamp() + "," + segments.get(i).getB());
              // end point of this segment
              double v =
                  (segments.get(i + 1).getInitTimestamp() - segments.get(i).getInitTimestamp())
                      * segments.get(i).getA() + segments.get(i).getB();
              writer.println(segments.get(i + 1).getInitTimestamp() + "," + v);
            }
            // the two end points of the last segment
            writer.println(
                segments.get(segments.size() - 1).getInitTimestamp() + "," + segments.get(
                    segments.size() - 1).getB());
            double v =
                (simPiece.lastTimeStamp - segments.get(segments.size() - 1).getInitTimestamp())
                    * segments.get(segments.size() - 1).getA() + segments.get(segments.size() - 1)
                    .getB();
            writer.println(simPiece.lastTimeStamp + "," + v);
          }
        } catch (
            Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

  public static double getSimPieceParam(int nout, TimeSeries ts, double accuracy)
      throws IOException {
    double epsilon = ts.range * 0.001;
    while (true) {
      SimPiece simPiece = new SimPiece(ts.data, epsilon);
      if (simPiece.segments.size() * 2 > nout) { // note *2 for disjoint
        epsilon *= 2;
      } else {
        break;
      }
    }
    double left = epsilon / 2;
    double right = epsilon;
    while (Math.abs(right - left) > accuracy) {
      double mid = (left + right) / 2;
      SimPiece simPiece = new SimPiece(ts.data, mid);
      if (simPiece.segments.size() * 2 > nout) { // note *2 for disjoint
        left = mid;
      } else {
        right = mid;
      }
    }
    return (left + right) / 2;
  }
}
