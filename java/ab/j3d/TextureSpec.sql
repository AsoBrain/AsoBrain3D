DROP TABLE IF EXISTS TextureSpecs;
CREATE TABLE TextureSpecs (
  ID bigint(20) DEFAULT '0' NOT NULL  auto_increment,
  code varchar(12) DEFAULT '' NOT NULL ,
  rgb int(10) DEFAULT '-1' NOT NULL ,
  textureScale decimal(8,3) DEFAULT '1.0',
  opacity decimal(8,6) DEFAULT '1.0',
  ambientReflectivity decimal(8,6) DEFAULT '0.3',
  diffuseReflectivity decimal(8,6) DEFAULT '0.5',
  specularReflectivity decimal(8,6) DEFAULT '0.7',
  specularExponent int(10) unsigned DEFAULT '8',
  grain enum('true','false') DEFAULT 'false' NOT NULL ,
  PRIMARY KEY (ID),
  UNIQUE KEY code (code)
);

INSERT INTO TextureSpecs (code,rgb,textureScale,ambientReflectivity,diffuseReflectivity,specularReflectivity,specularExponent,grain) VALUES
 ('metal'      ,      -1,NULL ,0.15,0.50,0.90,16,'false')
,('nickel'     ,      -1,NULL ,0.15,0.50,0.90,16,'false')
,('EV1'        ,0xF8F8F8,NULL ,0.25,0.40,0.80,16,'false')
,('sink'       ,      -1,NULL ,0.15,0.50,0.90,16,'false')
,('messing'    ,      -1,NULL ,0.15,0.50,0.90,16,'false')
,('beech'      ,0xE1DDC7,NULL ,0.30,0.30,0.30, 8,'false')
,('oak'        ,0xE1DDC7,NULL ,0.30,0.30,0.30, 8,'false')
,('wood'       ,0xE1DDC7,NULL ,0.30,0.30,0.30, 8,'false')
,('black'      ,0x010101,NULL ,0.10,0.15,0.90,16,'false')
,('lightgre'   ,0xF0F0F0,NULL ,0.20,0.20,0.30, 8,'false')
,('white'      ,0xFEFEFE,NULL ,0.30,0.30,0.30, 8,'false')
,('dark_brown' ,0x3D280D,NULL ,0.30,0.30,0.30, 8,'false')
,('brown'      ,0xC1815F,NULL ,0.30,0.30,0.30, 8,'false')
,('beige'      ,0xE1DDC7,NULL ,0.30,0.30,0.30, 8,'false')
,('transparent',      -1,NULL ,0.15,0.50,0.90, 8,'false')

,('TP_U254'    , 2570044,NULL ,0.20,0.30,0.30, 8,'false')
,('TP_U061'    ,10855849,NULL ,0.20,0.30,0.30, 8,'false')
;
