package ru.mws.link_shorter.exception;

public class LinkNotFoundException extends RuntimeException {
  public LinkNotFoundException(String message) {
    super(message);
  }
}
