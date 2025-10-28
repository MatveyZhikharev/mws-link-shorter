package ru.mws.link_shorter.exception;

public class LinkLengthOverflow extends RuntimeException {
  public LinkLengthOverflow(String message) {
    super(message);
  }
}
