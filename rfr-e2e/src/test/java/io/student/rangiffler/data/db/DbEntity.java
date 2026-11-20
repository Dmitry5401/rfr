package io.student.rangiffler.data.db;

public interface DbEntity {
  void create();

  void delete();

  boolean exists();
}
