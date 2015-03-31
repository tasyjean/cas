/**
 * Autogenerated by Thrift Compiler (0.8.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package com.inmobi.adserve.channels.adnetworks.ix;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;

import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Person implements org.apache.thrift.TBase<Person, Person._Fields>, java.io.Serializable, Cloneable {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("Person");

  private static final org.apache.thrift.protocol.TField FIRST_NAME_FIELD_DESC = new org.apache.thrift.protocol.TField("firstName", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField LAST_NAME_FIELD_DESC = new org.apache.thrift.protocol.TField("lastName", org.apache.thrift.protocol.TType.STRING, (short)2);
  private static final org.apache.thrift.protocol.TField ADDRESS_FIELD_DESC = new org.apache.thrift.protocol.TField("address", org.apache.thrift.protocol.TType.STRING, (short)3);
  private static final org.apache.thrift.protocol.TField MOBILE_FIELD_DESC = new org.apache.thrift.protocol.TField("mobile", org.apache.thrift.protocol.TType.I32, (short)4);
  private static final org.apache.thrift.protocol.TField ID_FIELD_DESC = new org.apache.thrift.protocol.TField("id", org.apache.thrift.protocol.TType.I32, (short)5);
  private static final org.apache.thrift.protocol.TField SECONDRY_PHONE_FIELD_DESC = new org.apache.thrift.protocol.TField("secondryPhone", org.apache.thrift.protocol.TType.I32, (short)6);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new PersonStandardSchemeFactory());
    schemes.put(TupleScheme.class, new PersonTupleSchemeFactory());
  }

  public String firstName; // required
  public String lastName; // required
  public String address; // optional
  public int mobile; // required
  public int id; // required
  public int secondryPhone; // optional

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    FIRST_NAME((short)1, "firstName"),
    LAST_NAME((short)2, "lastName"),
    ADDRESS((short)3, "address"),
    MOBILE((short)4, "mobile"),
    ID((short)5, "id"),
    SECONDRY_PHONE((short)6, "secondryPhone");

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
        case 1: // FIRST_NAME
          return FIRST_NAME;
        case 2: // LAST_NAME
          return LAST_NAME;
        case 3: // ADDRESS
          return ADDRESS;
        case 4: // MOBILE
          return MOBILE;
        case 5: // ID
          return ID;
        case 6: // SECONDRY_PHONE
          return SECONDRY_PHONE;
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
  private static final int __MOBILE_ISSET_ID = 0;
  private static final int __ID_ISSET_ID = 1;
  private static final int __SECONDRYPHONE_ISSET_ID = 2;
  private BitSet __isset_bit_vector = new BitSet(3);
  private _Fields optionals[] = {_Fields.ADDRESS,_Fields.SECONDRY_PHONE};
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.FIRST_NAME, new org.apache.thrift.meta_data.FieldMetaData("firstName", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.LAST_NAME, new org.apache.thrift.meta_data.FieldMetaData("lastName", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.ADDRESS, new org.apache.thrift.meta_data.FieldMetaData("address", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.MOBILE, new org.apache.thrift.meta_data.FieldMetaData("mobile", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.ID, new org.apache.thrift.meta_data.FieldMetaData("id", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.SECONDRY_PHONE, new org.apache.thrift.meta_data.FieldMetaData("secondryPhone", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(Person.class, metaDataMap);
  }

  public Person() {
  }

  public Person(
    String firstName,
    String lastName,
    int mobile,
    int id)
  {
    this();
    this.firstName = firstName;
    this.lastName = lastName;
    this.mobile = mobile;
    setMobileIsSet(true);
    this.id = id;
    setIdIsSet(true);
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public Person(Person other) {
    __isset_bit_vector.clear();
    __isset_bit_vector.or(other.__isset_bit_vector);
    if (other.isSetFirstName()) {
      this.firstName = other.firstName;
    }
    if (other.isSetLastName()) {
      this.lastName = other.lastName;
    }
    if (other.isSetAddress()) {
      this.address = other.address;
    }
    this.mobile = other.mobile;
    this.id = other.id;
    this.secondryPhone = other.secondryPhone;
  }

  public Person deepCopy() {
    return new Person(this);
  }

  @Override
  public void clear() {
    this.firstName = null;
    this.lastName = null;
    this.address = null;
    setMobileIsSet(false);
    this.mobile = 0;
    setIdIsSet(false);
    this.id = 0;
    setSecondryPhoneIsSet(false);
    this.secondryPhone = 0;
  }

  public String getFirstName() {
    return this.firstName;
  }

  public Person setFirstName(String firstName) {
    this.firstName = firstName;
    return this;
  }

  public void unsetFirstName() {
    this.firstName = null;
  }

  /** Returns true if field firstName is set (has been assigned a value) and false otherwise */
  public boolean isSetFirstName() {
    return this.firstName != null;
  }

  public void setFirstNameIsSet(boolean value) {
    if (!value) {
      this.firstName = null;
    }
  }

  public String getLastName() {
    return this.lastName;
  }

  public Person setLastName(String lastName) {
    this.lastName = lastName;
    return this;
  }

  public void unsetLastName() {
    this.lastName = null;
  }

  /** Returns true if field lastName is set (has been assigned a value) and false otherwise */
  public boolean isSetLastName() {
    return this.lastName != null;
  }

  public void setLastNameIsSet(boolean value) {
    if (!value) {
      this.lastName = null;
    }
  }

  public String getAddress() {
    return this.address;
  }

  public Person setAddress(String address) {
    this.address = address;
    return this;
  }

  public void unsetAddress() {
    this.address = null;
  }

  /** Returns true if field address is set (has been assigned a value) and false otherwise */
  public boolean isSetAddress() {
    return this.address != null;
  }

  public void setAddressIsSet(boolean value) {
    if (!value) {
      this.address = null;
    }
  }

  public int getMobile() {
    return this.mobile;
  }

  public Person setMobile(int mobile) {
    this.mobile = mobile;
    setMobileIsSet(true);
    return this;
  }

  public void unsetMobile() {
    __isset_bit_vector.clear(__MOBILE_ISSET_ID);
  }

  /** Returns true if field mobile is set (has been assigned a value) and false otherwise */
  public boolean isSetMobile() {
    return __isset_bit_vector.get(__MOBILE_ISSET_ID);
  }

  public void setMobileIsSet(boolean value) {
    __isset_bit_vector.set(__MOBILE_ISSET_ID, value);
  }

  public int getId() {
    return this.id;
  }

  public Person setId(int id) {
    this.id = id;
    setIdIsSet(true);
    return this;
  }

  public void unsetId() {
    __isset_bit_vector.clear(__ID_ISSET_ID);
  }

  /** Returns true if field id is set (has been assigned a value) and false otherwise */
  public boolean isSetId() {
    return __isset_bit_vector.get(__ID_ISSET_ID);
  }

  public void setIdIsSet(boolean value) {
    __isset_bit_vector.set(__ID_ISSET_ID, value);
  }

  public int getSecondryPhone() {
    return this.secondryPhone;
  }

  public Person setSecondryPhone(int secondryPhone) {
    this.secondryPhone = secondryPhone;
    setSecondryPhoneIsSet(true);
    return this;
  }

  public void unsetSecondryPhone() {
    __isset_bit_vector.clear(__SECONDRYPHONE_ISSET_ID);
  }

  /** Returns true if field secondryPhone is set (has been assigned a value) and false otherwise */
  public boolean isSetSecondryPhone() {
    return __isset_bit_vector.get(__SECONDRYPHONE_ISSET_ID);
  }

  public void setSecondryPhoneIsSet(boolean value) {
    __isset_bit_vector.set(__SECONDRYPHONE_ISSET_ID, value);
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case FIRST_NAME:
      if (value == null) {
        unsetFirstName();
      } else {
        setFirstName((String)value);
      }
      break;

    case LAST_NAME:
      if (value == null) {
        unsetLastName();
      } else {
        setLastName((String)value);
      }
      break;

    case ADDRESS:
      if (value == null) {
        unsetAddress();
      } else {
        setAddress((String)value);
      }
      break;

    case MOBILE:
      if (value == null) {
        unsetMobile();
      } else {
        setMobile((Integer)value);
      }
      break;

    case ID:
      if (value == null) {
        unsetId();
      } else {
        setId((Integer)value);
      }
      break;

    case SECONDRY_PHONE:
      if (value == null) {
        unsetSecondryPhone();
      } else {
        setSecondryPhone((Integer)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case FIRST_NAME:
      return getFirstName();

    case LAST_NAME:
      return getLastName();

    case ADDRESS:
      return getAddress();

    case MOBILE:
      return Integer.valueOf(getMobile());

    case ID:
      return Integer.valueOf(getId());

    case SECONDRY_PHONE:
      return Integer.valueOf(getSecondryPhone());

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case FIRST_NAME:
      return isSetFirstName();
    case LAST_NAME:
      return isSetLastName();
    case ADDRESS:
      return isSetAddress();
    case MOBILE:
      return isSetMobile();
    case ID:
      return isSetId();
    case SECONDRY_PHONE:
      return isSetSecondryPhone();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof Person)
      return this.equals((Person)that);
    return false;
  }

  public boolean equals(Person that) {
    if (that == null)
      return false;

    boolean this_present_firstName = true && this.isSetFirstName();
    boolean that_present_firstName = true && that.isSetFirstName();
    if (this_present_firstName || that_present_firstName) {
      if (!(this_present_firstName && that_present_firstName))
        return false;
      if (!this.firstName.equals(that.firstName))
        return false;
    }

    boolean this_present_lastName = true && this.isSetLastName();
    boolean that_present_lastName = true && that.isSetLastName();
    if (this_present_lastName || that_present_lastName) {
      if (!(this_present_lastName && that_present_lastName))
        return false;
      if (!this.lastName.equals(that.lastName))
        return false;
    }

    boolean this_present_address = true && this.isSetAddress();
    boolean that_present_address = true && that.isSetAddress();
    if (this_present_address || that_present_address) {
      if (!(this_present_address && that_present_address))
        return false;
      if (!this.address.equals(that.address))
        return false;
    }

    boolean this_present_mobile = true;
    boolean that_present_mobile = true;
    if (this_present_mobile || that_present_mobile) {
      if (!(this_present_mobile && that_present_mobile))
        return false;
      if (this.mobile != that.mobile)
        return false;
    }

    boolean this_present_id = true;
    boolean that_present_id = true;
    if (this_present_id || that_present_id) {
      if (!(this_present_id && that_present_id))
        return false;
      if (this.id != that.id)
        return false;
    }

    boolean this_present_secondryPhone = true && this.isSetSecondryPhone();
    boolean that_present_secondryPhone = true && that.isSetSecondryPhone();
    if (this_present_secondryPhone || that_present_secondryPhone) {
      if (!(this_present_secondryPhone && that_present_secondryPhone))
        return false;
      if (this.secondryPhone != that.secondryPhone)
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    HashCodeBuilder builder = new HashCodeBuilder();

    boolean present_firstName = true && (isSetFirstName());
    builder.append(present_firstName);
    if (present_firstName)
      builder.append(firstName);

    boolean present_lastName = true && (isSetLastName());
    builder.append(present_lastName);
    if (present_lastName)
      builder.append(lastName);

    boolean present_address = true && (isSetAddress());
    builder.append(present_address);
    if (present_address)
      builder.append(address);

    boolean present_mobile = true;
    builder.append(present_mobile);
    if (present_mobile)
      builder.append(mobile);

    boolean present_id = true;
    builder.append(present_id);
    if (present_id)
      builder.append(id);

    boolean present_secondryPhone = true && (isSetSecondryPhone());
    builder.append(present_secondryPhone);
    if (present_secondryPhone)
      builder.append(secondryPhone);

    return builder.toHashCode();
  }

  public int compareTo(Person other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;
    Person typedOther = (Person)other;

    lastComparison = Boolean.valueOf(isSetFirstName()).compareTo(typedOther.isSetFirstName());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetFirstName()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.firstName, typedOther.firstName);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetLastName()).compareTo(typedOther.isSetLastName());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetLastName()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.lastName, typedOther.lastName);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetAddress()).compareTo(typedOther.isSetAddress());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetAddress()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.address, typedOther.address);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetMobile()).compareTo(typedOther.isSetMobile());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetMobile()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.mobile, typedOther.mobile);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetId()).compareTo(typedOther.isSetId());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetId()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.id, typedOther.id);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetSecondryPhone()).compareTo(typedOther.isSetSecondryPhone());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetSecondryPhone()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.secondryPhone, typedOther.secondryPhone);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("Person(");
    boolean first = true;

    sb.append("firstName:");
    if (this.firstName == null) {
      sb.append("null");
    } else {
      sb.append(this.firstName);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("lastName:");
    if (this.lastName == null) {
      sb.append("null");
    } else {
      sb.append(this.lastName);
    }
    first = false;
    if (isSetAddress()) {
      if (!first) sb.append(", ");
      sb.append("address:");
      if (this.address == null) {
        sb.append("null");
      } else {
        sb.append(this.address);
      }
      first = false;
    }
    if (!first) sb.append(", ");
    sb.append("mobile:");
    sb.append(this.mobile);
    first = false;
    if (!first) sb.append(", ");
    sb.append("id:");
    sb.append(this.id);
    first = false;
    if (isSetSecondryPhone()) {
      if (!first) sb.append(", ");
      sb.append("secondryPhone:");
      sb.append(this.secondryPhone);
      first = false;
    }
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    if (firstName == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'firstName' was not present! Struct: " + toString());
    }
    if (lastName == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'lastName' was not present! Struct: " + toString());
    }
    // alas, we cannot check 'mobile' because it's a primitive and you chose the non-beans generator.
    // alas, we cannot check 'id' because it's a primitive and you chose the non-beans generator.
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    try {
      // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
      __isset_bit_vector = new BitSet(1);
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class PersonStandardSchemeFactory implements SchemeFactory {
    public PersonStandardScheme getScheme() {
      return new PersonStandardScheme();
    }
  }

  private static class PersonStandardScheme extends StandardScheme<Person> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, Person struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // FIRST_NAME
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.firstName = iprot.readString();
              struct.setFirstNameIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // LAST_NAME
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.lastName = iprot.readString();
              struct.setLastNameIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // ADDRESS
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.address = iprot.readString();
              struct.setAddressIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // MOBILE
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.mobile = iprot.readI32();
              struct.setMobileIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 5: // ID
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.id = iprot.readI32();
              struct.setIdIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 6: // SECONDRY_PHONE
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.secondryPhone = iprot.readI32();
              struct.setSecondryPhoneIsSet(true);
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
      if (!struct.isSetMobile()) {
        throw new org.apache.thrift.protocol.TProtocolException("Required field 'mobile' was not found in serialized data! Struct: " + toString());
      }
      if (!struct.isSetId()) {
        throw new org.apache.thrift.protocol.TProtocolException("Required field 'id' was not found in serialized data! Struct: " + toString());
      }
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, Person struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.firstName != null) {
        oprot.writeFieldBegin(FIRST_NAME_FIELD_DESC);
        oprot.writeString(struct.firstName);
        oprot.writeFieldEnd();
      }
      if (struct.lastName != null) {
        oprot.writeFieldBegin(LAST_NAME_FIELD_DESC);
        oprot.writeString(struct.lastName);
        oprot.writeFieldEnd();
      }
      if (struct.address != null) {
        if (struct.isSetAddress()) {
          oprot.writeFieldBegin(ADDRESS_FIELD_DESC);
          oprot.writeString(struct.address);
          oprot.writeFieldEnd();
        }
      }
      oprot.writeFieldBegin(MOBILE_FIELD_DESC);
      oprot.writeI32(struct.mobile);
      oprot.writeFieldEnd();
      oprot.writeFieldBegin(ID_FIELD_DESC);
      oprot.writeI32(struct.id);
      oprot.writeFieldEnd();
      if (struct.isSetSecondryPhone()) {
        oprot.writeFieldBegin(SECONDRY_PHONE_FIELD_DESC);
        oprot.writeI32(struct.secondryPhone);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class PersonTupleSchemeFactory implements SchemeFactory {
    public PersonTupleScheme getScheme() {
      return new PersonTupleScheme();
    }
  }

  private static class PersonTupleScheme extends TupleScheme<Person> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, Person struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      oprot.writeString(struct.firstName);
      oprot.writeString(struct.lastName);
      oprot.writeI32(struct.mobile);
      oprot.writeI32(struct.id);
      BitSet optionals = new BitSet();
      if (struct.isSetAddress()) {
        optionals.set(0);
      }
      if (struct.isSetSecondryPhone()) {
        optionals.set(1);
      }
      oprot.writeBitSet(optionals, 2);
      if (struct.isSetAddress()) {
        oprot.writeString(struct.address);
      }
      if (struct.isSetSecondryPhone()) {
        oprot.writeI32(struct.secondryPhone);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, Person struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      struct.firstName = iprot.readString();
      struct.setFirstNameIsSet(true);
      struct.lastName = iprot.readString();
      struct.setLastNameIsSet(true);
      struct.mobile = iprot.readI32();
      struct.setMobileIsSet(true);
      struct.id = iprot.readI32();
      struct.setIdIsSet(true);
      BitSet incoming = iprot.readBitSet(2);
      if (incoming.get(0)) {
        struct.address = iprot.readString();
        struct.setAddressIsSet(true);
      }
      if (incoming.get(1)) {
        struct.secondryPhone = iprot.readI32();
        struct.setSecondryPhoneIsSet(true);
      }
    }
  }

}
