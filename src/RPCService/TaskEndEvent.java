/**
 * Autogenerated by Thrift Compiler (0.9.2)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package RPCService;

import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;

import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.EncodingUtils;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.server.AbstractNonblockingServer.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import javax.annotation.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked"})
@Generated(value = "Autogenerated by Thrift Compiler (0.9.2)", date = "2017-2-16")
public class TaskEndEvent implements org.apache.thrift.TBase<TaskEndEvent, TaskEndEvent._Fields>, java.io.Serializable, Cloneable, Comparable<TaskEndEvent> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("TaskEndEvent");

  private static final org.apache.thrift.protocol.TField TASK_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("taskId", org.apache.thrift.protocol.TType.I64, (short)1);
  private static final org.apache.thrift.protocol.TField STAGE_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("stageId", org.apache.thrift.protocol.TType.I32, (short)2);
  private static final org.apache.thrift.protocol.TField JOB_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("jobId", org.apache.thrift.protocol.TType.I32, (short)3);
  private static final org.apache.thrift.protocol.TField APP_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("appId", org.apache.thrift.protocol.TType.STRING, (short)4);
  private static final org.apache.thrift.protocol.TField TIME_STAMP_FIELD_DESC = new org.apache.thrift.protocol.TField("timestamp", org.apache.thrift.protocol.TType.I64, (short)5);
  private static final org.apache.thrift.protocol.TField REASON_FIELD_DESC = new org.apache.thrift.protocol.TField("reason", org.apache.thrift.protocol.TType.STRING, (short)6);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new TaskEndEventStandardSchemeFactory());
    schemes.put(TupleScheme.class, new TaskEndEventTupleSchemeFactory());
  }

  public long taskId; // required
  public int stageId; // required
  public int jobId; // required
  public String appId; // required
  public long timeStamp; // required
  public String reason; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    TASK_ID((short)1, "taskId"),
    STAGE_ID((short)2, "stageId"),
    JOB_ID((short)3, "jobId"),
    APP_ID((short)4, "appId"),
    TIME_STAMP((short)5, "timestamp"),
    REASON((short)6, "reason");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // TASK_ID
          return TASK_ID;
        case 2: // STAGE_ID
          return STAGE_ID;
        case 3: // JOB_ID
          return JOB_ID;
        case 4: // APP_ID
          return APP_ID;
        case 5: // TIME_STAMP
          return TIME_STAMP;
        case 6: // REASON
          return REASON;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  private static final int __TASKID_ISSET_ID = 0;
  private static final int __STAGEID_ISSET_ID = 1;
  private static final int __JOBID_ISSET_ID = 2;
  private static final int __TIMESTAMP_ISSET_ID = 3;
  private byte __isset_bitfield = 0;
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.TASK_ID, new org.apache.thrift.meta_data.FieldMetaData("taskId", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
    tmpMap.put(_Fields.STAGE_ID, new org.apache.thrift.meta_data.FieldMetaData("stageId", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.JOB_ID, new org.apache.thrift.meta_data.FieldMetaData("jobId", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.APP_ID, new org.apache.thrift.meta_data.FieldMetaData("appId", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.TIME_STAMP, new org.apache.thrift.meta_data.FieldMetaData("timestamp", org.apache.thrift.TFieldRequirementType.DEFAULT,
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
    tmpMap.put(_Fields.REASON, new org.apache.thrift.meta_data.FieldMetaData("reason", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(TaskEndEvent.class, metaDataMap);
  }

  public TaskEndEvent() {
  }

  public TaskEndEvent(
    long taskId,
    int stageId,
    int jobId,
    String appId,
    long timeStamp,
    String reason)
  {
    this();
    this.taskId = taskId;
    setTaskIdIsSet(true);
    this.stageId = stageId;
    setStageIdIsSet(true);
    this.jobId = jobId;
    setJobIdIsSet(true);
    this.appId = appId;
    this.timeStamp = timeStamp;
    setTimeStampIsSet(true);
    this.reason = reason;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public TaskEndEvent(TaskEndEvent other) {
    __isset_bitfield = other.__isset_bitfield;
    this.taskId = other.taskId;
    this.stageId = other.stageId;
    this.jobId = other.jobId;
    if (other.isSetAppId()) {
      this.appId = other.appId;
    }
    this.timeStamp = other.timeStamp;
    if (other.isSetReason()) {
      this.reason = other.reason;
    }
  }

  public TaskEndEvent deepCopy() {
    return new TaskEndEvent(this);
  }

  @Override
  public void clear() {
    setTaskIdIsSet(false);
    this.taskId = 0;
    setStageIdIsSet(false);
    this.stageId = 0;
    setJobIdIsSet(false);
    this.jobId = 0;
    this.appId = null;
    setTimeStampIsSet(false);
    this.timeStamp = 0;
    this.reason = null;
  }

  public long getTaskId() {
    return this.taskId;
  }

  public TaskEndEvent setTaskId(long taskId) {
    this.taskId = taskId;
    setTaskIdIsSet(true);
    return this;
  }

  public void unsetTaskId() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __TASKID_ISSET_ID);
  }

  /** Returns true if field taskId is set (has been assigned a value) and false otherwise */
  public boolean isSetTaskId() {
    return EncodingUtils.testBit(__isset_bitfield, __TASKID_ISSET_ID);
  }

  public void setTaskIdIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __TASKID_ISSET_ID, value);
  }

  public int getStageId() {
    return this.stageId;
  }

  public TaskEndEvent setStageId(int stageId) {
    this.stageId = stageId;
    setStageIdIsSet(true);
    return this;
  }

  public void unsetStageId() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __STAGEID_ISSET_ID);
  }

  /** Returns true if field stageId is set (has been assigned a value) and false otherwise */
  public boolean isSetStageId() {
    return EncodingUtils.testBit(__isset_bitfield, __STAGEID_ISSET_ID);
  }

  public void setStageIdIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __STAGEID_ISSET_ID, value);
  }

  public int getJobId() {
    return this.jobId;
  }

  public TaskEndEvent setJobId(int jobId) {
    this.jobId = jobId;
    setJobIdIsSet(true);
    return this;
  }

  public void unsetJobId() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __JOBID_ISSET_ID);
  }

  /** Returns true if field jobId is set (has been assigned a value) and false otherwise */
  public boolean isSetJobId() {
    return EncodingUtils.testBit(__isset_bitfield, __JOBID_ISSET_ID);
  }

  public void setJobIdIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __JOBID_ISSET_ID, value);
  }

  public String getAppId() {
    return this.appId;
  }

  public TaskEndEvent setAppId(String appId) {
    this.appId = appId;
    return this;
  }

  public void unsetAppId() {
    this.appId = null;
  }

  /** Returns true if field appId is set (has been assigned a value) and false otherwise */
  public boolean isSetAppId() {
    return this.appId != null;
  }

  public void setAppIdIsSet(boolean value) {
    if (!value) {
      this.appId = null;
    }
  }

  public long getTimeStamp() {
    return this.timeStamp;
  }

  public TaskEndEvent setTimeStamp(long timeStamp) {
    this.timeStamp = timeStamp;
    setTimeStampIsSet(true);
    return this;
  }

  public void unsetTimeStamp() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __TIMESTAMP_ISSET_ID);
  }

  /** Returns true if field timestamp is set (has been assigned a value) and false otherwise */
  public boolean isSetTimeStamp() {
    return EncodingUtils.testBit(__isset_bitfield, __TIMESTAMP_ISSET_ID);
  }

  public void setTimeStampIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __TIMESTAMP_ISSET_ID, value);
  }

  public String getReason() {
    return this.reason;
  }

  public TaskEndEvent setReason(String reason) {
    this.reason = reason;
    return this;
  }

  public void unsetReason() {
    this.reason = null;
  }

  /** Returns true if field reason is set (has been assigned a value) and false otherwise */
  public boolean isSetReason() {
    return this.reason != null;
  }

  public void setReasonIsSet(boolean value) {
    if (!value) {
      this.reason = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case TASK_ID:
      if (value == null) {
        unsetTaskId();
      } else {
        setTaskId((Long)value);
      }
      break;

    case STAGE_ID:
      if (value == null) {
        unsetStageId();
      } else {
        setStageId((Integer)value);
      }
      break;

    case JOB_ID:
      if (value == null) {
        unsetJobId();
      } else {
        setJobId((Integer)value);
      }
      break;

    case APP_ID:
      if (value == null) {
        unsetAppId();
      } else {
        setAppId((String)value);
      }
      break;

    case TIME_STAMP:
      if (value == null) {
        unsetTimeStamp();
      } else {
        setTimeStamp((Long)value);
      }
      break;

    case REASON:
      if (value == null) {
        unsetReason();
      } else {
        setReason((String)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case TASK_ID:
      return Long.valueOf(getTaskId());

    case STAGE_ID:
      return Integer.valueOf(getStageId());

    case JOB_ID:
      return Integer.valueOf(getJobId());

    case APP_ID:
      return getAppId();

    case TIME_STAMP:
      return Long.valueOf(getTimeStamp());

    case REASON:
      return getReason();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case TASK_ID:
      return isSetTaskId();
    case STAGE_ID:
      return isSetStageId();
    case JOB_ID:
      return isSetJobId();
    case APP_ID:
      return isSetAppId();
    case TIME_STAMP:
      return isSetTimeStamp();
    case REASON:
      return isSetReason();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof TaskEndEvent)
      return this.equals((TaskEndEvent)that);
    return false;
  }

  public boolean equals(TaskEndEvent that) {
    if (that == null)
      return false;

    boolean this_present_taskId = true;
    boolean that_present_taskId = true;
    if (this_present_taskId || that_present_taskId) {
      if (!(this_present_taskId && that_present_taskId))
        return false;
      if (this.taskId != that.taskId)
        return false;
    }

    boolean this_present_stageId = true;
    boolean that_present_stageId = true;
    if (this_present_stageId || that_present_stageId) {
      if (!(this_present_stageId && that_present_stageId))
        return false;
      if (this.stageId != that.stageId)
        return false;
    }

    boolean this_present_jobId = true;
    boolean that_present_jobId = true;
    if (this_present_jobId || that_present_jobId) {
      if (!(this_present_jobId && that_present_jobId))
        return false;
      if (this.jobId != that.jobId)
        return false;
    }

    boolean this_present_appId = true && this.isSetAppId();
    boolean that_present_appId = true && that.isSetAppId();
    if (this_present_appId || that_present_appId) {
      if (!(this_present_appId && that_present_appId))
        return false;
      if (!this.appId.equals(that.appId))
        return false;
    }

    boolean this_present_timeStamp = true;
    boolean that_present_timeStamp = true;
    if (this_present_timeStamp || that_present_timeStamp) {
      if (!(this_present_timeStamp && that_present_timeStamp))
        return false;
      if (this.timeStamp != that.timeStamp)
        return false;
    }

    boolean this_present_reason = true && this.isSetReason();
    boolean that_present_reason = true && that.isSetReason();
    if (this_present_reason || that_present_reason) {
      if (!(this_present_reason && that_present_reason))
        return false;
      if (!this.reason.equals(that.reason))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    List<Object> list = new ArrayList<Object>();

    boolean present_taskId = true;
    list.add(present_taskId);
    if (present_taskId)
      list.add(taskId);

    boolean present_stageId = true;
    list.add(present_stageId);
    if (present_stageId)
      list.add(stageId);

    boolean present_jobId = true;
    list.add(present_jobId);
    if (present_jobId)
      list.add(jobId);

    boolean present_appId = true && (isSetAppId());
    list.add(present_appId);
    if (present_appId)
      list.add(appId);

    boolean present_timeStamp = true;
    list.add(present_timeStamp);
    if (present_timeStamp)
      list.add(timeStamp);

    boolean present_reason = true && (isSetReason());
    list.add(present_reason);
    if (present_reason)
      list.add(reason);

    return list.hashCode();
  }

  @Override
  public int compareTo(TaskEndEvent other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetTaskId()).compareTo(other.isSetTaskId());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetTaskId()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.taskId, other.taskId);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetStageId()).compareTo(other.isSetStageId());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetStageId()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.stageId, other.stageId);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetJobId()).compareTo(other.isSetJobId());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetJobId()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.jobId, other.jobId);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetAppId()).compareTo(other.isSetAppId());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetAppId()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.appId, other.appId);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetTimeStamp()).compareTo(other.isSetTimeStamp());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetTimeStamp()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.timeStamp, other.timeStamp);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetReason()).compareTo(other.isSetReason());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetReason()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.reason, other.reason);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws TException {
    schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws TException {
    schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("TaskEndEvent(");
    boolean first = true;

    sb.append("taskId:");
    sb.append(this.taskId);
    first = false;
    if (!first) sb.append(", ");
    sb.append("stageId:");
    sb.append(this.stageId);
    first = false;
    if (!first) sb.append(", ");
    sb.append("jobId:");
    sb.append(this.jobId);
    first = false;
    if (!first) sb.append(", ");
    sb.append("appId:");
    if (this.appId == null) {
      sb.append("null");
    } else {
      sb.append(this.appId);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("timestamp:");
    sb.append(this.timeStamp);
    first = false;
    if (!first) sb.append(", ");
    sb.append("reason:");
    if (this.reason == null) {
      sb.append("null");
    } else {
      sb.append(this.reason);
    }
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws TException {
    // check for required fields
    // check for sub-struct validity
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    try {
      // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
      __isset_bitfield = 0;
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class TaskEndEventStandardSchemeFactory implements SchemeFactory {
    public TaskEndEventStandardScheme getScheme() {
      return new TaskEndEventStandardScheme();
    }
  }

  private static class TaskEndEventStandardScheme extends StandardScheme<TaskEndEvent> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, TaskEndEvent struct) throws TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // TASK_ID
            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
              struct.taskId = iprot.readI64();
              struct.setTaskIdIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // STAGE_ID
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.stageId = iprot.readI32();
              struct.setStageIdIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // JOB_ID
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.jobId = iprot.readI32();
              struct.setJobIdIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // APP_ID
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.appId = iprot.readString();
              struct.setAppIdIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 5: // TIME_STAMP
            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
              struct.timeStamp = iprot.readI64();
              struct.setTimeStampIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 6: // REASON
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.reason = iprot.readString();
              struct.setReasonIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, TaskEndEvent struct) throws TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      oprot.writeFieldBegin(TASK_ID_FIELD_DESC);
      oprot.writeI64(struct.taskId);
      oprot.writeFieldEnd();
      oprot.writeFieldBegin(STAGE_ID_FIELD_DESC);
      oprot.writeI32(struct.stageId);
      oprot.writeFieldEnd();
      oprot.writeFieldBegin(JOB_ID_FIELD_DESC);
      oprot.writeI32(struct.jobId);
      oprot.writeFieldEnd();
      if (struct.appId != null) {
        oprot.writeFieldBegin(APP_ID_FIELD_DESC);
        oprot.writeString(struct.appId);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldBegin(TIME_STAMP_FIELD_DESC);
      oprot.writeI64(struct.timeStamp);
      oprot.writeFieldEnd();
      if (struct.reason != null) {
        oprot.writeFieldBegin(REASON_FIELD_DESC);
        oprot.writeString(struct.reason);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class TaskEndEventTupleSchemeFactory implements SchemeFactory {
    public TaskEndEventTupleScheme getScheme() {
      return new TaskEndEventTupleScheme();
    }
  }

  private static class TaskEndEventTupleScheme extends TupleScheme<TaskEndEvent> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, TaskEndEvent struct) throws TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      BitSet optionals = new BitSet();
      if (struct.isSetTaskId()) {
        optionals.set(0);
      }
      if (struct.isSetStageId()) {
        optionals.set(1);
      }
      if (struct.isSetJobId()) {
        optionals.set(2);
      }
      if (struct.isSetAppId()) {
        optionals.set(3);
      }
      if (struct.isSetTimeStamp()) {
        optionals.set(4);
      }
      if (struct.isSetReason()) {
        optionals.set(5);
      }
      oprot.writeBitSet(optionals, 6);
      if (struct.isSetTaskId()) {
        oprot.writeI64(struct.taskId);
      }
      if (struct.isSetStageId()) {
        oprot.writeI32(struct.stageId);
      }
      if (struct.isSetJobId()) {
        oprot.writeI32(struct.jobId);
      }
      if (struct.isSetAppId()) {
        oprot.writeString(struct.appId);
      }
      if (struct.isSetTimeStamp()) {
        oprot.writeI64(struct.timeStamp);
      }
      if (struct.isSetReason()) {
        oprot.writeString(struct.reason);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, TaskEndEvent struct) throws TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      BitSet incoming = iprot.readBitSet(6);
      if (incoming.get(0)) {
        struct.taskId = iprot.readI64();
        struct.setTaskIdIsSet(true);
      }
      if (incoming.get(1)) {
        struct.stageId = iprot.readI32();
        struct.setStageIdIsSet(true);
      }
      if (incoming.get(2)) {
        struct.jobId = iprot.readI32();
        struct.setJobIdIsSet(true);
      }
      if (incoming.get(3)) {
        struct.appId = iprot.readString();
        struct.setAppIdIsSet(true);
      }
      if (incoming.get(4)) {
        struct.timeStamp = iprot.readI64();
        struct.setTimeStampIsSet(true);
      }
      if (incoming.get(5)) {
        struct.reason = iprot.readString();
        struct.setReasonIsSet(true);
      }
    }
  }

}

