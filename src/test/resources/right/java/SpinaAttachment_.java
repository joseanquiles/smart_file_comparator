package com.telefonica.infa.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "EclipseLink-2.5.2.v20140319-rNA", date = "2020-04-26T13:33:18")
@StaticMetamodel(SpinaAttachment.class)
public class SpinaAttachment_ {

    public static volatile SingularAttribute<SpinaAttachment, BigDecimal> userIdCreatorParty;
    public static volatile SingularAttribute<SpinaAttachment, BigDecimal> attaIdAttachment;
    public static volatile ListAttribute<SpinaAttachment, SpinpSpMessageSpec> spinpSpMessageSpecs;
    public static volatile SingularAttribute<SpinaAttachment, Timestamp> audiTiUpdate;
    public static volatile SingularAttribute<SpinaAttachment, String> attaCoDocument;
    public static volatile SingularAttribute<SpinaAttachment, Timestamp> audiTiCreation;
    public static volatile SingularAttribute<SpinaAttachment, BigDecimal> userIdUpdaterParty;
    public static volatile ListAttribute<SpinaAttachment, SpinrSpInteractionAddlInfo> spinrSpInteractionAddlInfos;

}
