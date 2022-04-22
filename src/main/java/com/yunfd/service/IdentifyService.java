package com.yunfd.service;

public interface IdentifyService {
  void clearUserRedisInfoAndSaveData(String token);

  boolean checkValidity(String token);
}
