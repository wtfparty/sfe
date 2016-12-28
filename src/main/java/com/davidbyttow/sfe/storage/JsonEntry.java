package com.davidbyttow.sfe.storage;

import com.google.common.base.Throwables;
import com.davidbyttow.sfe.json.Json;
import com.davidbyttow.sfe.storage.entity.Jsons;

public abstract class JsonEntry<T> {
  private byte[] jsonData = Jsons.defaultValue();

  public void setJsonData(T data) {
    this.jsonData = Json.writeValueAsBytes(data);
  }

  public void setJsonData(byte[] jsonData) {
    this.jsonData = jsonData;
  }

  public byte[] getJsonData() {
    return jsonData;
  }

  public T getJsonDataAsObject(Class<T> type) {
    try {
      return jsonData != null ? Json.readValue(jsonData, type) : type.newInstance();
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }
}
