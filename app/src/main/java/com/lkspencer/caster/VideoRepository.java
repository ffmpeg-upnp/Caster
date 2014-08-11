package com.lkspencer.caster;

import android.os.AsyncTask;

import com.lkspencer.caster.datamodels.ClassDataModel;
import com.lkspencer.caster.datamodels.CurriculumDataModel;
import com.lkspencer.caster.datamodels.TopicDataModel;
import com.lkspencer.caster.datamodels.VideoDataModel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

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
  private int action;
  public ArrayList<CurriculumDataModel> curriculumsDataModels;
  public ArrayList<ClassDataModel> classDataModels;
  public ArrayList<TopicDataModel> topicDataModels;
  public ArrayList<VideoDataModel> videoDataModels;
  public static class Actions {
    public static final int GET_CURRICULUMS = 0;
    public static final int GET_CLASSES = 1;
    public static final int GET_TOPICS = 2;
    public static final int GET_VIDEOS = 3;
  }



  public void GetCurriculums() {
    Connection connection = null;
    PreparedStatement statement = null;
    ResultSet rs = null;
    try {
      curriculumsDataModels = new ArrayList<CurriculumDataModel>();
      Class.forName("com.mysql.jdbc.Driver");
      connection = DriverManager.getConnection("jdbc:mysql://us-cdbr-azure-west-a.cloudapp.net:3306/stvstakATbOqDhAx?user=b31446d5980666&password=786a82b3");
      statement = connection.prepareStatement(
              "select\n" +
              "  c.*\n" +
              "from\n" +
              "  Curriculums c");
      rs = statement.executeQuery();
      rs.first();
      do {
        CurriculumDataModel curriculum = new CurriculumDataModel();
        curriculum.CurriculumId = rs.getInt(1);
        curriculum.Name = rs.getString(2);
        curriculumsDataModels.add(curriculum);
      } while (rs.next());
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      Close(connection, statement, rs);
    }
  }

  public void GetClasses(int curriculumId) {
    Connection connection = null;
    PreparedStatement statement = null;
    ResultSet rs = null;
    try {
      Class.forName("com.mysql.jdbc.Driver");
      classDataModels = new ArrayList<ClassDataModel>();
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
      rs = statement.executeQuery();
      rs.first();
      do {
        ClassDataModel classDataModel = new ClassDataModel();
        classDataModel.ClassId = rs.getInt(1);
        classDataModel.Name = rs.getString(2);
        classDataModels.add(classDataModel);
      } while (rs.next());
    } catch (SQLException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } finally {
      Close(connection, statement, rs);
    }
  }

  public void GetTopics(int curriculumId, int classId) {
    Connection connection = null;
    PreparedStatement statement = null;
    ResultSet rs = null;
    try {
      topicDataModels = new ArrayList<TopicDataModel>();
      Class.forName("com.mysql.jdbc.Driver");
      connection = DriverManager.getConnection("jdbc:mysql://us-cdbr-azure-west-a.cloudapp.net:3306/stvstakATbOqDhAx?user=b31446d5980666&password=786a82b3");
      statement = null;
      if (curriculumId == 1) {
        statement = connection.prepareStatement(
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
      if (statement == null) return;
      rs = statement.executeQuery();

      rs.first();
      do {
        TopicDataModel topicDataModel = new TopicDataModel();
        topicDataModel.TopicId = rs.getInt(1);
        topicDataModel.Name = rs.getString(2);
        topicDataModels.add(topicDataModel);
      } while (rs.next());
      connection.close();
      statement.close();
      rs.close();
    } catch (SQLException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } finally {
      Close(connection, statement, rs);
    }
  }

  public void GetVideos(int curriculumId, int classId, int topicId) {
    Connection connection = null;
    PreparedStatement statement = null;
    ResultSet rs = null;
    try {
      videoDataModels = new ArrayList<VideoDataModel>();
      Class.forName("com.mysql.jdbc.Driver");
      connection = DriverManager.getConnection("jdbc:mysql://us-cdbr-azure-west-a.cloudapp.net:3306/stvstakATbOqDhAx?user=b31446d5980666&password=786a82b3");
      statement = null;
      if (curriculumId == 1) {
        statement = connection.prepareStatement(
                "select\n" +
                "  cfm.ComeFollowMeVideoId,\n" +
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
                "  and t.TopicId = ?"
        );
        statement.setInt(1, year);
        statement.setInt(2, month);
        statement.setInt(3, classId);
        statement.setInt(4, topicId);
      }
      if (statement == null) return;

      rs = statement.executeQuery();
      rs.first();
      do {
        VideoDataModel videoDataModel = new VideoDataModel();
        videoDataModel.VideoId = rs.getInt(1);
        videoDataModel.Name = rs.getString(2);
        videoDataModel.Link = rs.getString(3);
        videoDataModels.add(videoDataModel);
      } while (rs.next());
      connection.close();
      statement.close();
      rs.close();
    } catch (SQLException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } finally {
      Close(connection, statement, rs);
    }
  }

  private void Close(Connection connection, PreparedStatement statement, ResultSet rs) {
    try {
      if (connection != null) {
        connection.close();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    try {
      if (statement != null) {
        statement.close();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    try {
      if (rs != null) {
        rs.close();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }



  @Override protected Void doInBackground(Integer[]... params) {
    action = params[0][0];
    curriculumsDataModels = null;
    classDataModels = null;
    topicDataModels = null;
    videoDataModels = null;

    switch (action) {
      case Actions.GET_CURRICULUMS:
        GetCurriculums();
        break;
      case Actions.GET_CLASSES:
        GetClasses(params[0][1]);
        break;
      case Actions.GET_TOPICS:
        GetTopics(params[0][1], params[0][2]);
        break;
      case Actions.GET_VIDEOS:
        GetVideos(params[0][1], params[0][2], params[0][3]);
        break;
    }
    return null;
  }

  @Override protected void onPostExecute(Void aVoid) {
    switch (action) {
      case Actions.GET_CURRICULUMS:
        videoRepositoryCallback.ProcessCurriculums(this);
        break;
      case Actions.GET_CLASSES:
        videoRepositoryCallback.ProcessClasses(this);
        break;
      case Actions.GET_TOPICS:
        videoRepositoryCallback.ProcessTopics(this);
        break;
      case Actions.GET_VIDEOS:
        videoRepositoryCallback.ProcessVideos(this);
        break;
    }
    super.onPostExecute(aVoid);
  }

}
