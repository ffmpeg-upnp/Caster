package com.lkspencer.caster;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Created by Kirk on 8/10/2014.
 * This is used so that it can be handed off to the VideoRepository class
 */
public interface IVideoRepositoryCallback {
  void ProcessResultSet(Connection connection, Statement statement, ResultSet resultSet);
}
