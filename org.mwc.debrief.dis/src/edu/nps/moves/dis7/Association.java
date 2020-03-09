package edu.nps.moves.dis7;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.Serializable;

/**
 * An entity's associations with other entities and/or locations. For each
 * association, this record shall specify the type of the association, the
 * associated entity's EntityID and/or the associated location's world
 * coordinates. This record may be used (optionally) in a transfer transaction
 * to send internal state data from the divesting simulation to the acquiring
 * simulation (see 5.9.4). This record may also be used for other purposes.
 * Section 6.2.9
 *
 * Copyright (c) 2008-2016, MOVES Institute, Naval Postgraduate School. All
 * rights reserved. This work is licensed under the BSD open source license,
 * available at https://www.movesinstitute.org/licenses/bsd.html
 *
 * @author DMcG
 */
public class Association extends Object implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	protected short associationType;

	protected short padding4;

	/** identity of associated entity. If none, NO_SPECIFIC_ENTITY */
	protected EntityID associatedEntityID = new EntityID();

	/** location, in world coordinates */
	protected Vector3Double associatedLocation = new Vector3Double();

	/** Constructor */
	public Association() {
	}

	/*
	 * The equals method doesn't always work--mostly it works only on classes that
	 * consist only of primitives. Be careful.
	 */
	@Override
	public boolean equals(final Object obj) {

		if (this == obj) {
			return true;
		}

		if (obj == null) {
			return false;
		}

		if (getClass() != obj.getClass())
			return false;

		return equalsImpl(obj);
	}

	/**
	 * Compare all fields that contribute to the state, ignoring transient and
	 * static fields, for <code>this</code> and the supplied object
	 *
	 * @param obj the object to compare to
	 * @return true if the objects are equal, false otherwise.
	 */
	public boolean equalsImpl(final Object obj) {
		boolean ivarsEqual = true;

		if (!(obj instanceof Association))
			return false;

		final Association rhs = (Association) obj;

		if (!(associationType == rhs.associationType))
			ivarsEqual = false;
		if (!(padding4 == rhs.padding4))
			ivarsEqual = false;
		if (!(associatedEntityID.equals(rhs.associatedEntityID)))
			ivarsEqual = false;
		if (!(associatedLocation.equals(rhs.associatedLocation)))
			ivarsEqual = false;

		return ivarsEqual;
	}

	public EntityID getAssociatedEntityID() {
		return associatedEntityID;
	}

	public Vector3Double getAssociatedLocation() {
		return associatedLocation;
	}

	public short getAssociationType() {
		return associationType;
	}

	public int getMarshalledSize() {
		int marshalSize = 0;

		marshalSize = marshalSize + 1; // associationType
		marshalSize = marshalSize + 1; // padding4
		marshalSize = marshalSize + associatedEntityID.getMarshalledSize(); // associatedEntityID
		marshalSize = marshalSize + associatedLocation.getMarshalledSize(); // associatedLocation

		return marshalSize;
	}

	public short getPadding4() {
		return padding4;
	}

	public void marshal(final DataOutputStream dos) {
		try {
			dos.writeByte((byte) associationType);
			dos.writeByte((byte) padding4);
			associatedEntityID.marshal(dos);
			associatedLocation.marshal(dos);
		} // end try
		catch (final Exception e) {
			System.out.println(e);
		}
	} // end of marshal method

	/**
	 * Packs a Pdu into the ByteBuffer.
	 *
	 * @throws java.nio.BufferOverflowException if buff is too small
	 * @throws java.nio.ReadOnlyBufferException if buff is read only
	 * @see java.nio.ByteBuffer
	 * @param buff The ByteBuffer at the position to begin writing
	 * @since ??
	 */
	public void marshal(final java.nio.ByteBuffer buff) {
		buff.put((byte) associationType);
		buff.put((byte) padding4);
		associatedEntityID.marshal(buff);
		associatedLocation.marshal(buff);
	} // end of marshal method

	public void setAssociatedEntityID(final EntityID pAssociatedEntityID) {
		associatedEntityID = pAssociatedEntityID;
	}

	public void setAssociatedLocation(final Vector3Double pAssociatedLocation) {
		associatedLocation = pAssociatedLocation;
	}

	public void setAssociationType(final short pAssociationType) {
		associationType = pAssociationType;
	}

	public void setPadding4(final short pPadding4) {
		padding4 = pPadding4;
	}

	public void unmarshal(final DataInputStream dis) {
		try {
			associationType = (short) dis.readUnsignedByte();
			padding4 = (short) dis.readUnsignedByte();
			associatedEntityID.unmarshal(dis);
			associatedLocation.unmarshal(dis);
		} // end try
		catch (final Exception e) {
			System.out.println(e);
		}
	} // end of unmarshal method

	/**
	 * Unpacks a Pdu from the underlying data.
	 *
	 * @throws java.nio.BufferUnderflowException if buff is too small
	 * @see java.nio.ByteBuffer
	 * @param buff The ByteBuffer at the position to begin reading
	 * @since ??
	 */
	public void unmarshal(final java.nio.ByteBuffer buff) {
		associationType = (short) (buff.get() & 0xFF);
		padding4 = (short) (buff.get() & 0xFF);
		associatedEntityID.unmarshal(buff);
		associatedLocation.unmarshal(buff);
	} // end of unmarshal method
} // end of class
