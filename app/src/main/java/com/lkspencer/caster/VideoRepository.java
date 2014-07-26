package com.lkspencer.caster;

import android.os.AsyncTask;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by Kirk on 7/26/2014.
 * This class is used to query the list of videos from the MySql server.
 */
public class VideoRepository extends AsyncTask<Integer[], Void, Void> {

  public VideoRepository(Main main, int year, int month) {
    this.main = main;
    this.year = year;
    this.month = month;
  }

  private PreparedStatement preparedStatement = null;
  private Main main;
  private int year;
  private int month;

  public ResultSet GetVideos() {
    try {
      Class.forName("com.mysql.jdbc.Driver");
      try {
        Connection connect = DriverManager.getConnection("jdbc:mysql://us-cdbr-azure-west-a.cloudapp.net:3306/stvstakATbOqDhAx?user=b31446d5980666&password=786a82b3");
        // statements allow to issue SQL queries to the database
        Statement statement = connect.createStatement();
        ResultSet resultSet = statement.executeQuery(
                "select\n" +
                        "  c.Name as Class,\n" +
                        "  t.Name as Topic,\n" +
                        "  cfm.Year,\n" +
                        "  cfm.Month,\n" +
                        "  cfm.Link\n" +
                        "from\n" +
                        "  ComeFollowMeVideos cfm\n" +
                        "  inner join Classes c on cfm.ClassId = c.ClassId\n" +
                        "  inner join Topics t on cfm.TopicId = t.TopicId\n" +
                        "where\n" +
                        "  cfm.Year = " + year + "\n" +
                        "  and cfm.month = " + month);
        statement.close();
        connect.close();
        return resultSet;
      } catch (SQLException e) {
        e.printStackTrace();
      }
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override protected Void doInBackground(Integer[]... params) {
    main.displayVideos(GetVideos());
    return null;
  }

}
