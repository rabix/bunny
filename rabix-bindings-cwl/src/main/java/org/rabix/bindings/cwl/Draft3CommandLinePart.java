package org.rabix.bindings.draft3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.base.Preconditions;

/**
 * Command line part wrapper (simple/container)
 */
public class Draft3CommandLinePart {

  private int position;
  private boolean isFile;
  private List<Object> parts;

  private String keyValue;
  private int argsArrayOrder = -1;

  public boolean isFile() {
    return isFile;
  }

  public void setArgsArrayOrder(int argsArrayOrder) {
    this.argsArrayOrder = argsArrayOrder;
  }

  public void setKeyValue(String keyValue) {
    this.keyValue = keyValue;
  }

  public List<Object> flatten() {
    return flatten(parts);
  }

  private List<Object> flatten(List<Object> parts) {
    List<Object> flattened = new ArrayList<>();

    for (Object partObj : parts) {
      if (partObj instanceof Draft3CommandLinePart) {
        Draft3CommandLinePart part = (Draft3CommandLinePart) partObj;

        part.sort();
        flattened.addAll(flatten(part.parts));
      } else {
        flattened.add(partObj);
      }
    }
    return flattened;
  }

  public Draft3CommandLinePart sort() {
    Collections.sort(parts, new CommandLinePartComparator());
    return this;
  }

  public static class Builder {

    private int position;
    private boolean isFile;
    private List<Object> parts;

    private String keyValue;
    private int argsArrayOrder = -1;

    public Builder(int position, boolean isFile) {
      this.isFile = isFile;
      this.position = position;
      this.parts = new ArrayList<>();
    }

    public Builder position(int position) {
      this.position = position;
      return this;
    }

    public Builder isFile(boolean isFile) {
      this.isFile = isFile;
      return this;
    }

    public Builder part(Object part) {
      Preconditions.checkNotNull(part);
      this.parts.add(part);
      return this;
    }

    public Builder parts(List<Object> parts) {
      Preconditions.checkNotNull(parts);
      this.parts.addAll(parts);
      return this;
    }

    public Builder keyValue(String keyValue) {
      this.keyValue = keyValue;
      return this;
    }

    public Builder argsArrayOrder(int argsArrayOrder) {
      this.argsArrayOrder = argsArrayOrder;
      return this;
    }

    public Draft3CommandLinePart build() {
      Draft3CommandLinePart commandLinePart = new Draft3CommandLinePart();
      commandLinePart.position = position;
      commandLinePart.isFile = isFile;
      commandLinePart.parts = parts;
      commandLinePart.keyValue = keyValue;
      commandLinePart.argsArrayOrder = argsArrayOrder;
      return commandLinePart;
    }
  }

  public static class CommandLinePartComparator implements Comparator<Object> {

    public int compare(Object o1, Object o2) {
      if (o1 instanceof Draft3CommandLinePart && o2 instanceof Draft3CommandLinePart) {
        Draft3CommandLinePart part1 = (Draft3CommandLinePart) o1;
        Draft3CommandLinePart part2 = (Draft3CommandLinePart) o2;
        int positionDiff = part1.position - part2.position;
        
        if (positionDiff != 0) {
          return positionDiff;
        }
        return part1.keyValue.compareTo(part2.keyValue);
      }
      return 0;
    }

    public int compare(Draft3CommandLinePart o1, Draft3CommandLinePart o2) {
      if (o1 instanceof Draft3CommandLinePart && o2 instanceof Draft3CommandLinePart) {
        Draft3CommandLinePart clp1 = (Draft3CommandLinePart) o1;
        Draft3CommandLinePart clp2 = (Draft3CommandLinePart) o2;

        int result = clp1.position - clp2.position;
        if (result != 0) {
          return result;
        }
        if (clp1.argsArrayOrder != -1 && clp2.argsArrayOrder != -1) {
          return clp1.argsArrayOrder - clp2.argsArrayOrder;
        }
        if (clp1.keyValue == null || clp2.keyValue == null) {
          return 0;
        }
        return clp1.keyValue.compareTo(clp2.keyValue);
      }
      return 0;
    }

  }

  @Override
  public String toString() {
    return "CommandLinePart [position=" + position + ", isFile=" + isFile + ", parts=" + parts + ", keyValue="
        + keyValue + ", argsArrayOrder=" + argsArrayOrder + "]";
  }

}
