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

public class MySample3 {

  // java -jar MySample-jar-with-dependencies.jar  480 HouseTwenty_TEST false -1 0 "D:\\kdd\\lts-exp\\datasets\\%s.csv" "D:\\kdd\\lts-exp\\notebook\\SimPieceResults\\%s-%s-%s.csv" true
  public static void main(String[] args) {
//    int[] noutList = new int[]{320, 360, 400, 440, 480, 520, 560, 600, 640};
//    String[] datasetNames = new String[]{"FreezerRegularTrain_TEST", "HouseTwenty_TEST", "Lightning7_TEST",
//        "Mallat_TEST", "OliveOil_TEST", "Wine_TEST"};
//    boolean[] hasHeaderList = new boolean[]{false, false, false, false, false, false};
//    int[] Nlist = new int[]{-1, -1, -1, -1, -1, -1}; // -1 means read all
//    int[] startList = new int[]{0, 0, 0, 0, 0, 0};
//    String filename = "D:\\kdd\\lts-exp\\datasets\\%s.csv";
//    String csvFile = "D:\\kdd\\lts-exp\\notebook\\SimPieceResults\\%s-%s-%s.csv";

    int[] noutList = new int[]{300};
    String[] datasetNames = new String[]{"FreezerRegularTrain_TEST", "HouseTwenty_TEST", "Lightning7_TEST",
        "Mallat_TEST", "OliveOil_TEST", "Wine_TEST"};
    boolean[] hasHeaderList = new boolean[]{false, false, false, false, false, false};
    int[] Nlist = new int[]{-1, -1, -1, -1, -1, -1}; // -1 means read all
    int[] startList = new int[]{0, 0, 0, 0, 0, 0};
    String filename = "D:\\kdd\\lts-exp\\datasets\\%s.csv";
    String csvFile = "D:\\kdd\\lts-exp\\notebook\\SimPieceResults\\%s-%s-%s.csv";

    for (int k = 0; k < datasetNames.length; k++) {
      for (int j = 0; j < noutList.length; j++) {
        try (FileInputStream inputStream = new FileInputStream(
            String.format(filename, datasetNames[k]))) {
          String delimiter = ",";
          TimeSeries ts = TimeSeriesReader.getMyTimeSeries(inputStream, delimiter, false, Nlist[k],
              startList[k], hasHeaderList[k]);

          double epsilon = getSimPieceParam(noutList[j], ts, 1e-6);
//        System.out.println(epsilon);
          System.out.println(
              datasetNames[k] + ": n=" + Nlist[k] + ",m=" + noutList[j] + ",epsilon=" + epsilon);

          SimPiece simPiece = new SimPiece(ts.data, epsilon);
          List<SimPieceSegment> segments = simPiece.segments;
//      System.out.println(segments.size() * 2);
          segments.sort(Comparator.comparingLong(SimPieceSegment::getInitTimestamp));
          try (PrintWriter writer = new PrintWriter(
              new FileWriter(String.format(csvFile, datasetNames[k], Nlist[k], noutList[j])))) {
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
