/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package com.readrealm.order.event;

import org.apache.avro.specific.SpecificData;
import org.apache.avro.util.Utf8;
import org.apache.avro.message.BinaryMessageEncoder;
import org.apache.avro.message.BinaryMessageDecoder;
import org.apache.avro.message.SchemaStore;

@org.apache.avro.specific.AvroGenerated
public class OrderEvent extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  private static final long serialVersionUID = -9034582155552834950L;


  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"OrderEvent\",\"namespace\":\"com.readrealm.order.event\",\"fields\":[{\"name\":\"orderId\",\"type\":[\"null\",{\"type\":\"string\",\"avro.java.string\":\"String\"}],\"default\":null},{\"name\":\"userFirstName\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"userLastName\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"userEmail\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"totalAmount\",\"type\":{\"type\":\"bytes\",\"logicalType\":\"decimal\",\"precision\":20,\"scale\":2}},{\"name\":\"details\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"OrderDetails\",\"fields\":[{\"name\":\"isbn\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"quantity\",\"type\":\"int\"},{\"name\":\"unitPrice\",\"type\":{\"type\":\"bytes\",\"logicalType\":\"decimal\",\"precision\":20,\"scale\":2}}]}}},{\"name\":\"paymentStatus\",\"type\":{\"type\":\"enum\",\"name\":\"PaymentStatus\",\"symbols\":[\"PENDING\",\"COMPLETED\",\"FAILED\",\"REFUNDED\",\"CANCELED\",\"PROCESSING\",\"REQUIRES_PAYMENT_METHOD\"]}},{\"name\":\"createdDate\",\"type\":{\"type\":\"long\",\"logicalType\":\"timestamp-millis\"}},{\"name\":\"updatedDate\",\"type\":{\"type\":\"long\",\"logicalType\":\"timestamp-millis\"}}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }

  private static final SpecificData MODEL$ = new SpecificData();
  static {
    MODEL$.addLogicalTypeConversion(new org.apache.avro.data.TimeConversions.TimestampMillisConversion());
    MODEL$.addLogicalTypeConversion(new org.apache.avro.Conversions.DecimalConversion());
  }

  private static final BinaryMessageEncoder<OrderEvent> ENCODER =
      new BinaryMessageEncoder<>(MODEL$, SCHEMA$);

  private static final BinaryMessageDecoder<OrderEvent> DECODER =
      new BinaryMessageDecoder<>(MODEL$, SCHEMA$);

  /**
   * Return the BinaryMessageEncoder instance used by this class.
   * @return the message encoder used by this class
   */
  public static BinaryMessageEncoder<OrderEvent> getEncoder() {
    return ENCODER;
  }

  /**
   * Return the BinaryMessageDecoder instance used by this class.
   * @return the message decoder used by this class
   */
  public static BinaryMessageDecoder<OrderEvent> getDecoder() {
    return DECODER;
  }

  /**
   * Create a new BinaryMessageDecoder instance for this class that uses the specified {@link SchemaStore}.
   * @param resolver a {@link SchemaStore} used to find schemas by fingerprint
   * @return a BinaryMessageDecoder instance for this class backed by the given SchemaStore
   */
  public static BinaryMessageDecoder<OrderEvent> createDecoder(SchemaStore resolver) {
    return new BinaryMessageDecoder<>(MODEL$, SCHEMA$, resolver);
  }

  /**
   * Serializes this OrderEvent to a ByteBuffer.
   * @return a buffer holding the serialized data for this instance
   * @throws java.io.IOException if this instance could not be serialized
   */
  public java.nio.ByteBuffer toByteBuffer() throws java.io.IOException {
    return ENCODER.encode(this);
  }

  /**
   * Deserializes a OrderEvent from a ByteBuffer.
   * @param b a byte buffer holding serialized data for an instance of this class
   * @return a OrderEvent instance decoded from the given buffer
   * @throws java.io.IOException if the given bytes could not be deserialized into an instance of this class
   */
  public static OrderEvent fromByteBuffer(
      java.nio.ByteBuffer b) throws java.io.IOException {
    return DECODER.decode(b);
  }

  private java.lang.String orderId;
  private java.lang.String userFirstName;
  private java.lang.String userLastName;
  private java.lang.String userEmail;
  private java.math.BigDecimal totalAmount;
  private java.util.List<com.readrealm.order.event.OrderDetails> details;
  private com.readrealm.order.event.PaymentStatus paymentStatus;
  private java.time.Instant createdDate;
  private java.time.Instant updatedDate;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>.
   */
  public OrderEvent() {}

  /**
   * All-args constructor.
   * @param orderId The new value for orderId
   * @param userFirstName The new value for userFirstName
   * @param userLastName The new value for userLastName
   * @param userEmail The new value for userEmail
   * @param totalAmount The new value for totalAmount
   * @param details The new value for details
   * @param paymentStatus The new value for paymentStatus
   * @param createdDate The new value for createdDate
   * @param updatedDate The new value for updatedDate
   */
  public OrderEvent(java.lang.String orderId, java.lang.String userFirstName, java.lang.String userLastName, java.lang.String userEmail, java.math.BigDecimal totalAmount, java.util.List<com.readrealm.order.event.OrderDetails> details, com.readrealm.order.event.PaymentStatus paymentStatus, java.time.Instant createdDate, java.time.Instant updatedDate) {
    this.orderId = orderId;
    this.userFirstName = userFirstName;
    this.userLastName = userLastName;
    this.userEmail = userEmail;
    this.totalAmount = totalAmount;
    this.details = details;
    this.paymentStatus = paymentStatus;
    this.createdDate = createdDate.truncatedTo(java.time.temporal.ChronoUnit.MILLIS);
    this.updatedDate = updatedDate.truncatedTo(java.time.temporal.ChronoUnit.MILLIS);
  }

  @Override
  public org.apache.avro.specific.SpecificData getSpecificData() { return MODEL$; }

  @Override
  public org.apache.avro.Schema getSchema() { return SCHEMA$; }

  // Used by DatumWriter.  Applications should not call.
  @Override
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return orderId;
    case 1: return userFirstName;
    case 2: return userLastName;
    case 3: return userEmail;
    case 4: return totalAmount;
    case 5: return details;
    case 6: return paymentStatus;
    case 7: return createdDate;
    case 8: return updatedDate;
    default: throw new IndexOutOfBoundsException("Invalid index: " + field$);
    }
  }

  private static final org.apache.avro.Conversion<?>[] conversions =
      new org.apache.avro.Conversion<?>[] {
      null,
      null,
      null,
      null,
      new org.apache.avro.Conversions.DecimalConversion(),
      null,
      null,
      new org.apache.avro.data.TimeConversions.TimestampMillisConversion(),
      new org.apache.avro.data.TimeConversions.TimestampMillisConversion(),
      null
  };

  @Override
  public org.apache.avro.Conversion<?> getConversion(int field) {
    return conversions[field];
  }

  // Used by DatumReader.  Applications should not call.
  @Override
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: orderId = value$ != null ? value$.toString() : null; break;
    case 1: userFirstName = value$ != null ? value$.toString() : null; break;
    case 2: userLastName = value$ != null ? value$.toString() : null; break;
    case 3: userEmail = value$ != null ? value$.toString() : null; break;
    case 4: totalAmount = (java.math.BigDecimal)value$; break;
    case 5: details = (java.util.List<com.readrealm.order.event.OrderDetails>)value$; break;
    case 6: paymentStatus = (com.readrealm.order.event.PaymentStatus)value$; break;
    case 7: createdDate = (java.time.Instant)value$; break;
    case 8: updatedDate = (java.time.Instant)value$; break;
    default: throw new IndexOutOfBoundsException("Invalid index: " + field$);
    }
  }

  /**
   * Gets the value of the 'orderId' field.
   * @return The value of the 'orderId' field.
   */
  public java.lang.String getOrderId() {
    return orderId;
  }


  /**
   * Sets the value of the 'orderId' field.
   * @param value the value to set.
   */
  public void setOrderId(java.lang.String value) {
    this.orderId = value;
  }

  /**
   * Gets the value of the 'userFirstName' field.
   * @return The value of the 'userFirstName' field.
   */
  public java.lang.String getUserFirstName() {
    return userFirstName;
  }


  /**
   * Sets the value of the 'userFirstName' field.
   * @param value the value to set.
   */
  public void setUserFirstName(java.lang.String value) {
    this.userFirstName = value;
  }

  /**
   * Gets the value of the 'userLastName' field.
   * @return The value of the 'userLastName' field.
   */
  public java.lang.String getUserLastName() {
    return userLastName;
  }


  /**
   * Sets the value of the 'userLastName' field.
   * @param value the value to set.
   */
  public void setUserLastName(java.lang.String value) {
    this.userLastName = value;
  }

  /**
   * Gets the value of the 'userEmail' field.
   * @return The value of the 'userEmail' field.
   */
  public java.lang.String getUserEmail() {
    return userEmail;
  }


  /**
   * Sets the value of the 'userEmail' field.
   * @param value the value to set.
   */
  public void setUserEmail(java.lang.String value) {
    this.userEmail = value;
  }

  /**
   * Gets the value of the 'totalAmount' field.
   * @return The value of the 'totalAmount' field.
   */
  public java.math.BigDecimal getTotalAmount() {
    return totalAmount;
  }


  /**
   * Sets the value of the 'totalAmount' field.
   * @param value the value to set.
   */
  public void setTotalAmount(java.math.BigDecimal value) {
    this.totalAmount = value;
  }

  /**
   * Gets the value of the 'details' field.
   * @return The value of the 'details' field.
   */
  public java.util.List<com.readrealm.order.event.OrderDetails> getDetails() {
    return details;
  }


  /**
   * Sets the value of the 'details' field.
   * @param value the value to set.
   */
  public void setDetails(java.util.List<com.readrealm.order.event.OrderDetails> value) {
    this.details = value;
  }

  /**
   * Gets the value of the 'paymentStatus' field.
   * @return The value of the 'paymentStatus' field.
   */
  public com.readrealm.order.event.PaymentStatus getPaymentStatus() {
    return paymentStatus;
  }


  /**
   * Sets the value of the 'paymentStatus' field.
   * @param value the value to set.
   */
  public void setPaymentStatus(com.readrealm.order.event.PaymentStatus value) {
    this.paymentStatus = value;
  }

  /**
   * Gets the value of the 'createdDate' field.
   * @return The value of the 'createdDate' field.
   */
  public java.time.Instant getCreatedDate() {
    return createdDate;
  }


  /**
   * Sets the value of the 'createdDate' field.
   * @param value the value to set.
   */
  public void setCreatedDate(java.time.Instant value) {
    this.createdDate = value.truncatedTo(java.time.temporal.ChronoUnit.MILLIS);
  }

  /**
   * Gets the value of the 'updatedDate' field.
   * @return The value of the 'updatedDate' field.
   */
  public java.time.Instant getUpdatedDate() {
    return updatedDate;
  }


  /**
   * Sets the value of the 'updatedDate' field.
   * @param value the value to set.
   */
  public void setUpdatedDate(java.time.Instant value) {
    this.updatedDate = value.truncatedTo(java.time.temporal.ChronoUnit.MILLIS);
  }

  /**
   * Creates a new OrderEvent RecordBuilder.
   * @return A new OrderEvent RecordBuilder
   */
  public static com.readrealm.order.event.OrderEvent.Builder newBuilder() {
    return new com.readrealm.order.event.OrderEvent.Builder();
  }

  /**
   * Creates a new OrderEvent RecordBuilder by copying an existing Builder.
   * @param other The existing builder to copy.
   * @return A new OrderEvent RecordBuilder
   */
  public static com.readrealm.order.event.OrderEvent.Builder newBuilder(com.readrealm.order.event.OrderEvent.Builder other) {
    if (other == null) {
      return new com.readrealm.order.event.OrderEvent.Builder();
    } else {
      return new com.readrealm.order.event.OrderEvent.Builder(other);
    }
  }

  /**
   * Creates a new OrderEvent RecordBuilder by copying an existing OrderEvent instance.
   * @param other The existing instance to copy.
   * @return A new OrderEvent RecordBuilder
   */
  public static com.readrealm.order.event.OrderEvent.Builder newBuilder(com.readrealm.order.event.OrderEvent other) {
    if (other == null) {
      return new com.readrealm.order.event.OrderEvent.Builder();
    } else {
      return new com.readrealm.order.event.OrderEvent.Builder(other);
    }
  }

  /**
   * RecordBuilder for OrderEvent instances.
   */
  @org.apache.avro.specific.AvroGenerated
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<OrderEvent>
    implements org.apache.avro.data.RecordBuilder<OrderEvent> {

    private java.lang.String orderId;
    private java.lang.String userFirstName;
    private java.lang.String userLastName;
    private java.lang.String userEmail;
    private java.math.BigDecimal totalAmount;
    private java.util.List<com.readrealm.order.event.OrderDetails> details;
    private com.readrealm.order.event.PaymentStatus paymentStatus;
    private java.time.Instant createdDate;
    private java.time.Instant updatedDate;

    /** Creates a new Builder */
    private Builder() {
      super(SCHEMA$, MODEL$);
    }

    /**
     * Creates a Builder by copying an existing Builder.
     * @param other The existing Builder to copy.
     */
    private Builder(com.readrealm.order.event.OrderEvent.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.orderId)) {
        this.orderId = data().deepCopy(fields()[0].schema(), other.orderId);
        fieldSetFlags()[0] = other.fieldSetFlags()[0];
      }
      if (isValidValue(fields()[1], other.userFirstName)) {
        this.userFirstName = data().deepCopy(fields()[1].schema(), other.userFirstName);
        fieldSetFlags()[1] = other.fieldSetFlags()[1];
      }
      if (isValidValue(fields()[2], other.userLastName)) {
        this.userLastName = data().deepCopy(fields()[2].schema(), other.userLastName);
        fieldSetFlags()[2] = other.fieldSetFlags()[2];
      }
      if (isValidValue(fields()[3], other.userEmail)) {
        this.userEmail = data().deepCopy(fields()[3].schema(), other.userEmail);
        fieldSetFlags()[3] = other.fieldSetFlags()[3];
      }
      if (isValidValue(fields()[4], other.totalAmount)) {
        this.totalAmount = data().deepCopy(fields()[4].schema(), other.totalAmount);
        fieldSetFlags()[4] = other.fieldSetFlags()[4];
      }
      if (isValidValue(fields()[5], other.details)) {
        this.details = data().deepCopy(fields()[5].schema(), other.details);
        fieldSetFlags()[5] = other.fieldSetFlags()[5];
      }
      if (isValidValue(fields()[6], other.paymentStatus)) {
        this.paymentStatus = data().deepCopy(fields()[6].schema(), other.paymentStatus);
        fieldSetFlags()[6] = other.fieldSetFlags()[6];
      }
      if (isValidValue(fields()[7], other.createdDate)) {
        this.createdDate = data().deepCopy(fields()[7].schema(), other.createdDate);
        fieldSetFlags()[7] = other.fieldSetFlags()[7];
      }
      if (isValidValue(fields()[8], other.updatedDate)) {
        this.updatedDate = data().deepCopy(fields()[8].schema(), other.updatedDate);
        fieldSetFlags()[8] = other.fieldSetFlags()[8];
      }
    }

    /**
     * Creates a Builder by copying an existing OrderEvent instance
     * @param other The existing instance to copy.
     */
    private Builder(com.readrealm.order.event.OrderEvent other) {
      super(SCHEMA$, MODEL$);
      if (isValidValue(fields()[0], other.orderId)) {
        this.orderId = data().deepCopy(fields()[0].schema(), other.orderId);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.userFirstName)) {
        this.userFirstName = data().deepCopy(fields()[1].schema(), other.userFirstName);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.userLastName)) {
        this.userLastName = data().deepCopy(fields()[2].schema(), other.userLastName);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.userEmail)) {
        this.userEmail = data().deepCopy(fields()[3].schema(), other.userEmail);
        fieldSetFlags()[3] = true;
      }
      if (isValidValue(fields()[4], other.totalAmount)) {
        this.totalAmount = data().deepCopy(fields()[4].schema(), other.totalAmount);
        fieldSetFlags()[4] = true;
      }
      if (isValidValue(fields()[5], other.details)) {
        this.details = data().deepCopy(fields()[5].schema(), other.details);
        fieldSetFlags()[5] = true;
      }
      if (isValidValue(fields()[6], other.paymentStatus)) {
        this.paymentStatus = data().deepCopy(fields()[6].schema(), other.paymentStatus);
        fieldSetFlags()[6] = true;
      }
      if (isValidValue(fields()[7], other.createdDate)) {
        this.createdDate = data().deepCopy(fields()[7].schema(), other.createdDate);
        fieldSetFlags()[7] = true;
      }
      if (isValidValue(fields()[8], other.updatedDate)) {
        this.updatedDate = data().deepCopy(fields()[8].schema(), other.updatedDate);
        fieldSetFlags()[8] = true;
      }
    }

    /**
      * Gets the value of the 'orderId' field.
      * @return The value.
      */
    public java.lang.String getOrderId() {
      return orderId;
    }


    /**
      * Sets the value of the 'orderId' field.
      * @param value The value of 'orderId'.
      * @return This builder.
      */
    public com.readrealm.order.event.OrderEvent.Builder setOrderId(java.lang.String value) {
      validate(fields()[0], value);
      this.orderId = value;
      fieldSetFlags()[0] = true;
      return this;
    }

    /**
      * Checks whether the 'orderId' field has been set.
      * @return True if the 'orderId' field has been set, false otherwise.
      */
    public boolean hasOrderId() {
      return fieldSetFlags()[0];
    }


    /**
      * Clears the value of the 'orderId' field.
      * @return This builder.
      */
    public com.readrealm.order.event.OrderEvent.Builder clearOrderId() {
      orderId = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /**
      * Gets the value of the 'userFirstName' field.
      * @return The value.
      */
    public java.lang.String getUserFirstName() {
      return userFirstName;
    }


    /**
      * Sets the value of the 'userFirstName' field.
      * @param value The value of 'userFirstName'.
      * @return This builder.
      */
    public com.readrealm.order.event.OrderEvent.Builder setUserFirstName(java.lang.String value) {
      validate(fields()[1], value);
      this.userFirstName = value;
      fieldSetFlags()[1] = true;
      return this;
    }

    /**
      * Checks whether the 'userFirstName' field has been set.
      * @return True if the 'userFirstName' field has been set, false otherwise.
      */
    public boolean hasUserFirstName() {
      return fieldSetFlags()[1];
    }


    /**
      * Clears the value of the 'userFirstName' field.
      * @return This builder.
      */
    public com.readrealm.order.event.OrderEvent.Builder clearUserFirstName() {
      userFirstName = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    /**
      * Gets the value of the 'userLastName' field.
      * @return The value.
      */
    public java.lang.String getUserLastName() {
      return userLastName;
    }


    /**
      * Sets the value of the 'userLastName' field.
      * @param value The value of 'userLastName'.
      * @return This builder.
      */
    public com.readrealm.order.event.OrderEvent.Builder setUserLastName(java.lang.String value) {
      validate(fields()[2], value);
      this.userLastName = value;
      fieldSetFlags()[2] = true;
      return this;
    }

    /**
      * Checks whether the 'userLastName' field has been set.
      * @return True if the 'userLastName' field has been set, false otherwise.
      */
    public boolean hasUserLastName() {
      return fieldSetFlags()[2];
    }


    /**
      * Clears the value of the 'userLastName' field.
      * @return This builder.
      */
    public com.readrealm.order.event.OrderEvent.Builder clearUserLastName() {
      userLastName = null;
      fieldSetFlags()[2] = false;
      return this;
    }

    /**
      * Gets the value of the 'userEmail' field.
      * @return The value.
      */
    public java.lang.String getUserEmail() {
      return userEmail;
    }


    /**
      * Sets the value of the 'userEmail' field.
      * @param value The value of 'userEmail'.
      * @return This builder.
      */
    public com.readrealm.order.event.OrderEvent.Builder setUserEmail(java.lang.String value) {
      validate(fields()[3], value);
      this.userEmail = value;
      fieldSetFlags()[3] = true;
      return this;
    }

    /**
      * Checks whether the 'userEmail' field has been set.
      * @return True if the 'userEmail' field has been set, false otherwise.
      */
    public boolean hasUserEmail() {
      return fieldSetFlags()[3];
    }


    /**
      * Clears the value of the 'userEmail' field.
      * @return This builder.
      */
    public com.readrealm.order.event.OrderEvent.Builder clearUserEmail() {
      userEmail = null;
      fieldSetFlags()[3] = false;
      return this;
    }

    /**
      * Gets the value of the 'totalAmount' field.
      * @return The value.
      */
    public java.math.BigDecimal getTotalAmount() {
      return totalAmount;
    }


    /**
      * Sets the value of the 'totalAmount' field.
      * @param value The value of 'totalAmount'.
      * @return This builder.
      */
    public com.readrealm.order.event.OrderEvent.Builder setTotalAmount(java.math.BigDecimal value) {
      validate(fields()[4], value);
      this.totalAmount = value;
      fieldSetFlags()[4] = true;
      return this;
    }

    /**
      * Checks whether the 'totalAmount' field has been set.
      * @return True if the 'totalAmount' field has been set, false otherwise.
      */
    public boolean hasTotalAmount() {
      return fieldSetFlags()[4];
    }


    /**
      * Clears the value of the 'totalAmount' field.
      * @return This builder.
      */
    public com.readrealm.order.event.OrderEvent.Builder clearTotalAmount() {
      totalAmount = null;
      fieldSetFlags()[4] = false;
      return this;
    }

    /**
      * Gets the value of the 'details' field.
      * @return The value.
      */
    public java.util.List<com.readrealm.order.event.OrderDetails> getDetails() {
      return details;
    }


    /**
      * Sets the value of the 'details' field.
      * @param value The value of 'details'.
      * @return This builder.
      */
    public com.readrealm.order.event.OrderEvent.Builder setDetails(java.util.List<com.readrealm.order.event.OrderDetails> value) {
      validate(fields()[5], value);
      this.details = value;
      fieldSetFlags()[5] = true;
      return this;
    }

    /**
      * Checks whether the 'details' field has been set.
      * @return True if the 'details' field has been set, false otherwise.
      */
    public boolean hasDetails() {
      return fieldSetFlags()[5];
    }


    /**
      * Clears the value of the 'details' field.
      * @return This builder.
      */
    public com.readrealm.order.event.OrderEvent.Builder clearDetails() {
      details = null;
      fieldSetFlags()[5] = false;
      return this;
    }

    /**
      * Gets the value of the 'paymentStatus' field.
      * @return The value.
      */
    public com.readrealm.order.event.PaymentStatus getPaymentStatus() {
      return paymentStatus;
    }


    /**
      * Sets the value of the 'paymentStatus' field.
      * @param value The value of 'paymentStatus'.
      * @return This builder.
      */
    public com.readrealm.order.event.OrderEvent.Builder setPaymentStatus(com.readrealm.order.event.PaymentStatus value) {
      validate(fields()[6], value);
      this.paymentStatus = value;
      fieldSetFlags()[6] = true;
      return this;
    }

    /**
      * Checks whether the 'paymentStatus' field has been set.
      * @return True if the 'paymentStatus' field has been set, false otherwise.
      */
    public boolean hasPaymentStatus() {
      return fieldSetFlags()[6];
    }


    /**
      * Clears the value of the 'paymentStatus' field.
      * @return This builder.
      */
    public com.readrealm.order.event.OrderEvent.Builder clearPaymentStatus() {
      paymentStatus = null;
      fieldSetFlags()[6] = false;
      return this;
    }

    /**
      * Gets the value of the 'createdDate' field.
      * @return The value.
      */
    public java.time.Instant getCreatedDate() {
      return createdDate;
    }


    /**
      * Sets the value of the 'createdDate' field.
      * @param value The value of 'createdDate'.
      * @return This builder.
      */
    public com.readrealm.order.event.OrderEvent.Builder setCreatedDate(java.time.Instant value) {
      validate(fields()[7], value);
      this.createdDate = value.truncatedTo(java.time.temporal.ChronoUnit.MILLIS);
      fieldSetFlags()[7] = true;
      return this;
    }

    /**
      * Checks whether the 'createdDate' field has been set.
      * @return True if the 'createdDate' field has been set, false otherwise.
      */
    public boolean hasCreatedDate() {
      return fieldSetFlags()[7];
    }


    /**
      * Clears the value of the 'createdDate' field.
      * @return This builder.
      */
    public com.readrealm.order.event.OrderEvent.Builder clearCreatedDate() {
      fieldSetFlags()[7] = false;
      return this;
    }

    /**
      * Gets the value of the 'updatedDate' field.
      * @return The value.
      */
    public java.time.Instant getUpdatedDate() {
      return updatedDate;
    }


    /**
      * Sets the value of the 'updatedDate' field.
      * @param value The value of 'updatedDate'.
      * @return This builder.
      */
    public com.readrealm.order.event.OrderEvent.Builder setUpdatedDate(java.time.Instant value) {
      validate(fields()[8], value);
      this.updatedDate = value.truncatedTo(java.time.temporal.ChronoUnit.MILLIS);
      fieldSetFlags()[8] = true;
      return this;
    }

    /**
      * Checks whether the 'updatedDate' field has been set.
      * @return True if the 'updatedDate' field has been set, false otherwise.
      */
    public boolean hasUpdatedDate() {
      return fieldSetFlags()[8];
    }


    /**
      * Clears the value of the 'updatedDate' field.
      * @return This builder.
      */
    public com.readrealm.order.event.OrderEvent.Builder clearUpdatedDate() {
      fieldSetFlags()[8] = false;
      return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public OrderEvent build() {
      try {
        OrderEvent record = new OrderEvent();
        record.orderId = fieldSetFlags()[0] ? this.orderId : (java.lang.String) defaultValue(fields()[0]);
        record.userFirstName = fieldSetFlags()[1] ? this.userFirstName : (java.lang.String) defaultValue(fields()[1]);
        record.userLastName = fieldSetFlags()[2] ? this.userLastName : (java.lang.String) defaultValue(fields()[2]);
        record.userEmail = fieldSetFlags()[3] ? this.userEmail : (java.lang.String) defaultValue(fields()[3]);
        record.totalAmount = fieldSetFlags()[4] ? this.totalAmount : (java.math.BigDecimal) defaultValue(fields()[4]);
        record.details = fieldSetFlags()[5] ? this.details : (java.util.List<com.readrealm.order.event.OrderDetails>) defaultValue(fields()[5]);
        record.paymentStatus = fieldSetFlags()[6] ? this.paymentStatus : (com.readrealm.order.event.PaymentStatus) defaultValue(fields()[6]);
        record.createdDate = fieldSetFlags()[7] ? this.createdDate : (java.time.Instant) defaultValue(fields()[7]);
        record.updatedDate = fieldSetFlags()[8] ? this.updatedDate : (java.time.Instant) defaultValue(fields()[8]);
        return record;
      } catch (org.apache.avro.AvroMissingFieldException e) {
        throw e;
      } catch (java.lang.Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumWriter<OrderEvent>
    WRITER$ = (org.apache.avro.io.DatumWriter<OrderEvent>)MODEL$.createDatumWriter(SCHEMA$);

  @Override public void writeExternal(java.io.ObjectOutput out)
    throws java.io.IOException {
    WRITER$.write(this, SpecificData.getEncoder(out));
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumReader<OrderEvent>
    READER$ = (org.apache.avro.io.DatumReader<OrderEvent>)MODEL$.createDatumReader(SCHEMA$);

  @Override public void readExternal(java.io.ObjectInput in)
    throws java.io.IOException {
    READER$.read(this, SpecificData.getDecoder(in));
  }

}










