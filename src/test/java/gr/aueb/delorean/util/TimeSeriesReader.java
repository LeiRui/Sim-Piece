package gr.aueb.delorean.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

public class TimeSeriesReader {

  public static TimeSeries getTimeSeries(InputStream inputStream, String delimiter, boolean gzip) {
    ArrayList<Point> ts = new ArrayList<>();
    double max = Double.MIN_VALUE;
    double min = Double.MAX_VALUE;

    try {
      if (gzip) {
        inputStream = new GZIPInputStream(inputStream);
      }
      Reader decoder = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
      BufferedReader bufferedReader = new BufferedReader(decoder);

      String line;
      while ((line = bufferedReader.readLine()) != null) {
        String[] elements = line.split(delimiter);
        long timestamp = Long.parseLong(elements[0]);
        double value = Double.parseDouble(elements[1]);
        ts.add(new Point(timestamp, value));

        max = Math.max(max, value);
        min = Math.min(min, value);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return new TimeSeries(ts, max - min);
  }

  public static TimeSeries getMyTimeSeries(InputStream inputStream, String delimiter, boolean gzip,
      int N, boolean hasHeader) {
    ArrayList<Point> ts = new ArrayList<>();
    double max = Double.MIN_VALUE;
    double min = Double.MAX_VALUE;

    try {
      if (gzip) {
        inputStream = new GZIPInputStream(inputStream);
      }
      Reader decoder = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
      BufferedReader bufferedReader = new BufferedReader(decoder);

      String line;
      if (hasHeader) {
        bufferedReader.readLine();
      }
      int cnt = 0;
      // N<0 means read all lines
      while ((cnt < N || N < 0) && (line = bufferedReader.readLine()) != null) {
        String[] elements = line.split(delimiter);
//        long timestamp = Long.parseLong(elements[0]);
        long timestamp = (long) Double.parseDouble(elements[0]);
        double value = Double.parseDouble(elements[1]);
        ts.add(new Point(timestamp, value));

        max = Math.max(max, value);
        min = Math.min(min, value);
        cnt++;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return new TimeSeries(ts, max - min);
  }
}
