package ru.mws.link_shorter.exception;

public class LinkIsInvalid extends RuntimeException {
  public LinkIsInvalid(String message) {
    super(message);
  }
}
