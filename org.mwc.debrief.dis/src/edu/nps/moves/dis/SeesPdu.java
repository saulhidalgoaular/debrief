package edu.nps.moves.dis;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Section 5.3.7.5. SEES PDU, supplemental emissions entity state information.
 * COMPLETE
 *
 * Copyright (c) 2008-2016, MOVES Institute, Naval Postgraduate School. All
 * rights reserved. This work is licensed under the BSD open source license,
 * available at https://www.movesinstitute.org/licenses/bsd.html
 *
 * @author DMcG
 */
public class SeesPdu extends DistributedEmissionsFamilyPdu implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/** Originating entity ID */
	protected EntityID orginatingEntityID = new EntityID();

	/** IR Signature representation index */
	protected int infraredSignatureRepresentationIndex;

	/** acoustic Signature representation index */
	protected int acousticSignatureRepresentationIndex;

	/** radar cross section representation index */
	protected int radarCrossSectionSignatureRepresentationIndex;

	/** how many propulsion systems */
	protected int numberOfPropulsionSystems;

	/** how many vectoring nozzle systems */
	protected int numberOfVectoringNozzleSystems;

	/** variable length list of propulsion system data */
	protected List<PropulsionSystemData> propulsionSystemData = new ArrayList<PropulsionSystemData>();
	/** variable length list of vectoring system data */
	protected List<VectoringNozzleSystemData> vectoringSystemData = new ArrayList<VectoringNozzleSystemData>();

	/** Constructor */
	public SeesPdu() {
		setPduType((short) 30);
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

	@Override
	public boolean equalsImpl(final Object obj) {
		boolean ivarsEqual = true;

		if (!(obj instanceof SeesPdu))
			return false;

		final SeesPdu rhs = (SeesPdu) obj;

		if (!(orginatingEntityID.equals(rhs.orginatingEntityID)))
			ivarsEqual = false;
		if (!(infraredSignatureRepresentationIndex == rhs.infraredSignatureRepresentationIndex))
			ivarsEqual = false;
		if (!(acousticSignatureRepresentationIndex == rhs.acousticSignatureRepresentationIndex))
			ivarsEqual = false;
		if (!(radarCrossSectionSignatureRepresentationIndex == rhs.radarCrossSectionSignatureRepresentationIndex))
			ivarsEqual = false;
		if (!(numberOfPropulsionSystems == rhs.numberOfPropulsionSystems))
			ivarsEqual = false;
		if (!(numberOfVectoringNozzleSystems == rhs.numberOfVectoringNozzleSystems))
			ivarsEqual = false;

		for (int idx = 0; idx < propulsionSystemData.size(); idx++) {
			if (!(propulsionSystemData.get(idx).equals(rhs.propulsionSystemData.get(idx))))
				ivarsEqual = false;
		}

		for (int idx = 0; idx < vectoringSystemData.size(); idx++) {
			if (!(vectoringSystemData.get(idx).equals(rhs.vectoringSystemData.get(idx))))
				ivarsEqual = false;
		}

		return ivarsEqual && super.equalsImpl(rhs);
	}

	public int getAcousticSignatureRepresentationIndex() {
		return acousticSignatureRepresentationIndex;
	}

	public int getInfraredSignatureRepresentationIndex() {
		return infraredSignatureRepresentationIndex;
	}

	@Override
	public int getMarshalledSize() {
		int marshalSize = 0;

		marshalSize = super.getMarshalledSize();
		marshalSize = marshalSize + orginatingEntityID.getMarshalledSize(); // orginatingEntityID
		marshalSize = marshalSize + 2; // infraredSignatureRepresentationIndex
		marshalSize = marshalSize + 2; // acousticSignatureRepresentationIndex
		marshalSize = marshalSize + 2; // radarCrossSectionSignatureRepresentationIndex
		marshalSize = marshalSize + 2; // numberOfPropulsionSystems
		marshalSize = marshalSize + 2; // numberOfVectoringNozzleSystems
		for (int idx = 0; idx < propulsionSystemData.size(); idx++) {
			final PropulsionSystemData listElement = propulsionSystemData.get(idx);
			marshalSize = marshalSize + listElement.getMarshalledSize();
		}
		for (int idx = 0; idx < vectoringSystemData.size(); idx++) {
			final VectoringNozzleSystemData listElement = vectoringSystemData.get(idx);
			marshalSize = marshalSize + listElement.getMarshalledSize();
		}

		return marshalSize;
	}

	public int getNumberOfPropulsionSystems() {
		return propulsionSystemData.size();
	}

	public int getNumberOfVectoringNozzleSystems() {
		return vectoringSystemData.size();
	}

	public EntityID getOrginatingEntityID() {
		return orginatingEntityID;
	}

	public List<PropulsionSystemData> getPropulsionSystemData() {
		return propulsionSystemData;
	}

	public int getRadarCrossSectionSignatureRepresentationIndex() {
		return radarCrossSectionSignatureRepresentationIndex;
	}

	public List<VectoringNozzleSystemData> getVectoringSystemData() {
		return vectoringSystemData;
	}

	@Override
	public void marshal(final DataOutputStream dos) {
		super.marshal(dos);
		try {
			orginatingEntityID.marshal(dos);
			dos.writeShort((short) infraredSignatureRepresentationIndex);
			dos.writeShort((short) acousticSignatureRepresentationIndex);
			dos.writeShort((short) radarCrossSectionSignatureRepresentationIndex);
			dos.writeShort((short) propulsionSystemData.size());
			dos.writeShort((short) vectoringSystemData.size());

			for (int idx = 0; idx < propulsionSystemData.size(); idx++) {
				final PropulsionSystemData aPropulsionSystemData = propulsionSystemData.get(idx);
				aPropulsionSystemData.marshal(dos);
			} // end of list marshalling

			for (int idx = 0; idx < vectoringSystemData.size(); idx++) {
				final VectoringNozzleSystemData aVectoringNozzleSystemData = vectoringSystemData.get(idx);
				aVectoringNozzleSystemData.marshal(dos);
			} // end of list marshalling

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
	@Override
	public void marshal(final java.nio.ByteBuffer buff) {
		super.marshal(buff);
		orginatingEntityID.marshal(buff);
		buff.putShort((short) infraredSignatureRepresentationIndex);
		buff.putShort((short) acousticSignatureRepresentationIndex);
		buff.putShort((short) radarCrossSectionSignatureRepresentationIndex);
		buff.putShort((short) propulsionSystemData.size());
		buff.putShort((short) vectoringSystemData.size());

		for (int idx = 0; idx < propulsionSystemData.size(); idx++) {
			final PropulsionSystemData aPropulsionSystemData = propulsionSystemData.get(idx);
			aPropulsionSystemData.marshal(buff);
		} // end of list marshalling

		for (int idx = 0; idx < vectoringSystemData.size(); idx++) {
			final VectoringNozzleSystemData aVectoringNozzleSystemData = vectoringSystemData.get(idx);
			aVectoringNozzleSystemData.marshal(buff);
		} // end of list marshalling

	} // end of marshal method

	public void setAcousticSignatureRepresentationIndex(final int pAcousticSignatureRepresentationIndex) {
		acousticSignatureRepresentationIndex = pAcousticSignatureRepresentationIndex;
	}

	public void setInfraredSignatureRepresentationIndex(final int pInfraredSignatureRepresentationIndex) {
		infraredSignatureRepresentationIndex = pInfraredSignatureRepresentationIndex;
	}

	/**
	 * Note that setting this value will not change the marshalled value. The list
	 * whose length this describes is used for that purpose. The
	 * getnumberOfPropulsionSystems method will also be based on the actual list
	 * length rather than this value. The method is simply here for java bean
	 * completeness.
	 */
	public void setNumberOfPropulsionSystems(final int pNumberOfPropulsionSystems) {
		numberOfPropulsionSystems = pNumberOfPropulsionSystems;
	}

	/**
	 * Note that setting this value will not change the marshalled value. The list
	 * whose length this describes is used for that purpose. The
	 * getnumberOfVectoringNozzleSystems method will also be based on the actual
	 * list length rather than this value. The method is simply here for java bean
	 * completeness.
	 */
	public void setNumberOfVectoringNozzleSystems(final int pNumberOfVectoringNozzleSystems) {
		numberOfVectoringNozzleSystems = pNumberOfVectoringNozzleSystems;
	}

	public void setOrginatingEntityID(final EntityID pOrginatingEntityID) {
		orginatingEntityID = pOrginatingEntityID;
	}

	public void setPropulsionSystemData(final List<PropulsionSystemData> pPropulsionSystemData) {
		propulsionSystemData = pPropulsionSystemData;
	}

	public void setRadarCrossSectionSignatureRepresentationIndex(
			final int pRadarCrossSectionSignatureRepresentationIndex) {
		radarCrossSectionSignatureRepresentationIndex = pRadarCrossSectionSignatureRepresentationIndex;
	}

	public void setVectoringSystemData(final List<VectoringNozzleSystemData> pVectoringSystemData) {
		vectoringSystemData = pVectoringSystemData;
	}

	@Override
	public void unmarshal(final DataInputStream dis) {
		super.unmarshal(dis);

		try {
			orginatingEntityID.unmarshal(dis);
			infraredSignatureRepresentationIndex = dis.readUnsignedShort();
			acousticSignatureRepresentationIndex = dis.readUnsignedShort();
			radarCrossSectionSignatureRepresentationIndex = dis.readUnsignedShort();
			numberOfPropulsionSystems = dis.readUnsignedShort();
			numberOfVectoringNozzleSystems = dis.readUnsignedShort();
			for (int idx = 0; idx < numberOfPropulsionSystems; idx++) {
				final PropulsionSystemData anX = new PropulsionSystemData();
				anX.unmarshal(dis);
				propulsionSystemData.add(anX);
			}

			for (int idx = 0; idx < numberOfVectoringNozzleSystems; idx++) {
				final VectoringNozzleSystemData anX = new VectoringNozzleSystemData();
				anX.unmarshal(dis);
				vectoringSystemData.add(anX);
			}

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
	@Override
	public void unmarshal(final java.nio.ByteBuffer buff) {
		super.unmarshal(buff);

		orginatingEntityID.unmarshal(buff);
		infraredSignatureRepresentationIndex = buff.getShort() & 0xFFFF;
		acousticSignatureRepresentationIndex = buff.getShort() & 0xFFFF;
		radarCrossSectionSignatureRepresentationIndex = buff.getShort() & 0xFFFF;
		numberOfPropulsionSystems = buff.getShort() & 0xFFFF;
		numberOfVectoringNozzleSystems = buff.getShort() & 0xFFFF;
		for (int idx = 0; idx < numberOfPropulsionSystems; idx++) {
			final PropulsionSystemData anX = new PropulsionSystemData();
			anX.unmarshal(buff);
			propulsionSystemData.add(anX);
		}

		for (int idx = 0; idx < numberOfVectoringNozzleSystems; idx++) {
			final VectoringNozzleSystemData anX = new VectoringNozzleSystemData();
			anX.unmarshal(buff);
			vectoringSystemData.add(anX);
		}

	} // end of unmarshal method
} // end of class
