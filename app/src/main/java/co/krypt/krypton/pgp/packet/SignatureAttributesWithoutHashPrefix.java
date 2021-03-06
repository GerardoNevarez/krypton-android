package co.krypt.krypton.pgp.packet;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import co.krypt.krypton.pgp.publickey.PublicKeyAlgorithm;
import co.krypt.krypton.pgp.publickey.UnsupportedPublicKeyAlgorithmException;
import co.krypt.krypton.pgp.subpacket.DuplicateSubpacketException;
import co.krypt.krypton.pgp.subpacket.InvalidSubpacketLengthException;
import co.krypt.krypton.pgp.subpacket.SubpacketList;
import co.krypt.krypton.pgp.subpacket.UnsupportedCriticalSubpacketTypeException;

/**
 * Created by Kevin King on 6/12/17.
 * Copyright 2017. KryptCo, Inc.
 */

public class SignatureAttributesWithoutHashPrefix extends Serializable implements Signable {
    public static final byte VERSION = 4;
    public final SignatureType type;
    public final PublicKeyAlgorithm pkAlgorithm;
    public final HashAlgorithm hashAlgorithm;
    public final SubpacketList hashedSubpackets;
    public final SubpacketList unhashedSubpackets;

    public SignatureAttributesWithoutHashPrefix(SignatureType type, PublicKeyAlgorithm pkAlgorithm, HashAlgorithm hashAlgorithm, SubpacketList hashedSubpackets, SubpacketList unhashedSubpackets) throws IOException, NoSuchAlgorithmException {
        this.type = type;
        this.pkAlgorithm = pkAlgorithm;
        this.hashAlgorithm = hashAlgorithm;
        this.hashedSubpackets = hashedSubpackets;
        this.unhashedSubpackets = unhashedSubpackets;
    }

    public static SignatureAttributesWithoutHashPrefix parse(DataInputStream in) throws UnsupportedSignatureVersionException, IOException, UnsupportedPublicKeyAlgorithmException, InvalidSubpacketLengthException, UnsupportedCriticalSubpacketTypeException, UnsupportedHashAlgorithmException, NoSuchAlgorithmException, DuplicateSubpacketException {
        if (in.readByte() != VERSION) {
            throw new UnsupportedSignatureVersionException();
        }
        return new SignatureAttributesWithoutHashPrefix(
                SignatureType.parse(in),
                PublicKeyAlgorithm.parse(in.readByte()),
                HashAlgorithm.parse(in.readByte()),
                SubpacketList.parse(in),
                SubpacketList.parse(in)
        );
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        out.writeByte(VERSION);
        type.serialize(out);
        pkAlgorithm.serialize(out);
        hashAlgorithm.serialize(out);
        hashedSubpackets.serialize(out);
        unhashedSubpackets.serialize(out);
    }

    @Override
    public void writeSignableData(DataOutputStream out) throws IOException {
        ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
        DataOutputStream buf = new DataOutputStream(outBuf);

        buf.writeByte(VERSION);
        type.serialize(buf);
        pkAlgorithm.serialize(buf);
        hashAlgorithm.serialize(buf);
        hashedSubpackets.serialize(buf);

        buf.close();

        byte[] preTrailerData = outBuf.toByteArray();

        out.write(preTrailerData);
        out.writeByte(VERSION);
        out.writeByte(0xFF);
        out.writeInt(preTrailerData.length);
    }

    public HashAlgorithm getHashAlgorithm() {
        return hashAlgorithm;
    }
}
