DROP TABLE IF EXISTS Materials;
CREATE TABLE Materials
(
  ID                 BIGINT(20)           NOT NULL auto_increment,
  code               VARCHAR(64)          NOT NULL ,
  ambientColorRed    DECIMAL(8,6)         NOT NULL DEFAULT '0.0' ,
  ambientColorGreen  DECIMAL(8,6)         NOT NULL DEFAULT '0.0' ,
  ambientColorBlue   DECIMAL(8,6)         NOT NULL DEFAULT '0.0' ,
  diffuseColorRed    DECIMAL(8,6)         NOT NULL DEFAULT '0.0' ,
  diffuseColorGreen  DECIMAL(8,6)         NOT NULL DEFAULT '0.0' ,
  diffuseColorBlue   DECIMAL(8,6)         NOT NULL DEFAULT '0.0' ,
  diffuseColorAlpha  DECIMAL(8,6)         NOT NULL DEFAULT '0.0' ,
  specularColorGreen DECIMAL(8,6)         NOT NULL DEFAULT '0.0' ,
  specularColorBlue  DECIMAL(8,6)         NOT NULL DEFAULT '0.0' ,
  specularColorAlpha DECIMAL(8,6)         NOT NULL DEFAULT '0.0' ,
  specularExponent   INT(10) UNSIGNED     NOT NULL DEFAULT '16' ,
  colorMap           VARCHAR(64)                   DEFAULT NULL ,
  colorMapWidth      DECIMAL(8,6)         NOT NULL DEFAULT '0.0',
  colorMapHeight     DECIMAL(8,6)         NOT NULL DEFAULT '0.0',
  grain              ENUM('true','false') NOT NULL DEFAULT 'false' ,
  PRIMARY KEY (ID),
  UNIQUE KEY code (code)
);

