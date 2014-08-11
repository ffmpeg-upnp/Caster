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

  public VideoRepository(IVideoRepositoryCallback videoRepositoryCallback, int year, int month) {
    this.videoRepositoryCallback = videoRepositoryCallback;
    this.year = year;
    this.month = month;
  }



  private IVideoRepositoryCallback videoRepositoryCallback;
  private int year;
  private int month;
  private Connection connection;
  private PreparedStatement statement;
  private ResultSet resultSet;
  public static class Actions {
    public static final int GET_CURRICULUMS = 0;
    public static final int GET_CLASSES = 1;
    public static final int GET_TOPICS = 2;
    public static final int GET_VIDEOS = 3;
  };



  public ResultSet GetCurriculums() {
    try {
      Class.forName("com.mysql.jdbc.Driver");
      try {
        connection = DriverManager.getConnection("jdbc:mysql://us-cdbr-azure-west-a.cloudapp.net:3306/stvstakATbOqDhAx?user=b31446d5980666&password=786a82b3");
        statement = connection.prepareStatement(
                "select\n" +
                "  c.*\n" +
                "from\n" +
                "  Curriculums c");
        return statement.executeQuery();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    return null;
  }

  public ResultSet GetClasses(int curriculumId) {
    try {
      Class.forName("com.mysql.jdbc.Driver");
      try {
        connection = DriverManager.getConnection("jdbc:mysql://us-cdbr-azure-west-a.cloudapp.net:3306/stvstakATbOqDhAx?user=b31446d5980666&password=786a82b3");
        statement = connection.prepareStatement(
                "select\n" +
                        "  c.*\n" +
                        "from\n" +
                        "  Classes c\n" +
                        "where\n" +
                        "  c.CurriculumId = ?\n"
        );
        statement.setInt(1, curriculumId);
        return statement.executeQuery();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    return null;
  }

  public ResultSet GetTopics(int curriculumId, int classId) {
    try {
      Class.forName("com.mysql.jdbc.Driver");
      try {
        Connection connect = DriverManager.getConnection("jdbc:mysql://us-cdbr-azure-west-a.cloudapp.net:3306/stvstakATbOqDhAx?user=b31446d5980666&password=786a82b3");
        PreparedStatement statement = null;
        if (curriculumId == 1) {
          statement = connect.prepareStatement(
                  "select distinct\n" +
                  "  t.*\n" +
                  "from\n" +
                  "  ComeFollowMeVideos cfm\n" +
                  "  inner join Classes c on cfm.ClassId = c.ClassId\n" +
                  "  inner join Topics t on cfm.TopicId = t.TopicId\n" +
                  "where\n" +
                  "  cfm.Year = ?\n" +
                  "  and cfm.month = ?\n" +
                  "  and c.ClassId = ?\n"
          );
          statement.setInt(1, year);
          statement.setInt(2, month);
          statement.setInt(3, classId);
        }
        if (statement == null) return null;
        return statement.executeQuery();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    return null;
  }

  public ResultSet GetVideos(int curriculumId, int classId, int topicId) {
    try {
      Class.forName("com.mysql.jdbc.Driver");
      try {
        Connection connect = DriverManager.getConnection("jdbc:mysql://us-cdbr-azure-west-a.cloudapp.net:3306/stvstakATbOqDhAx?user=b31446d5980666&password=786a82b3");
        PreparedStatement statement = null;
        if (curriculumId == 1) {
          statement = connect.prepareStatement(
                  "select\n" +
                  "  cfm.VideoName,\n" +
                  "  cfm.Link\n" +
                  "from\n" +
                  "  ComeFollowMeVideos cfm\n" +
                  "  inner join Classes c on cfm.ClassId = c.ClassId\n" +
                  "  inner join Topics t on cfm.TopicId = t.TopicId\n" +
                  "where\n" +
                  "  cfm.Year = ?\n" +
                  "  and cfm.month = ?\n" +
                  "  and c.ClassId = ?\n" +
                  "  and c.TopicId = ?"
          );
          statement.setInt(1, year);
          statement.setInt(2, month);
          statement.setInt(3, classId);
          statement.setInt(4, topicId);
        }
        if (statement == null) return null;

        return statement.executeQuery();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    return null;
  }

  public void Close() {
    try {
      if (this.connection != null) {
        this.connection.close();
      }
      if (this.statement != null) {
        this.statement.close();
      }
      if (this.resultSet != null) {
        this.resultSet.close();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }



  @Override protected Void doInBackground(Integer[]... params) {
    int action = params[0][0];

    switch (action) {
      case Actions.GET_CURRICULUMS:
        resultSet = GetCurriculums();
        break;
      case Actions.GET_CLASSES:
        resultSet = GetClasses(params[0][1]);
        break;
      case Actions.GET_TOPICS:
        resultSet = GetTopics(params[0][1], params[0][2]);
        break;
      case Actions.GET_VIDEOS:
        resultSet = GetVideos(params[0][1], params[0][2], params[0][3]);
        break;
    }
    return null;
  }

  @Override protected void onPostExecute(Void aVoid) {
    videoRepositoryCallback.ProcessResultSet(this, resultSet);
    super.onPostExecute(aVoid);
  }

}
