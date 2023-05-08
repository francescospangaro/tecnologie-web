-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema casa_daste
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema casa_daste
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `casa_daste` DEFAULT CHARACTER SET utf8 ;
USE `casa_daste` ;

-- -----------------------------------------------------
-- Table `casa_daste`.`utente`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `casa_daste`.`utente` (
  `idUtente` INT NOT NULL AUTO_INCREMENT,
  `nome` VARCHAR(45) NOT NULL,
  `cognome` VARCHAR(45) NOT NULL,
  `email` VARCHAR(45) NOT NULL,
  `password` VARCHAR(45) NOT NULL,
  `indirizzo` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`idUtente`),
  UNIQUE INDEX `email_UNIQUE` (`email` ASC) VISIBLE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `casa_daste`.`articolo`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `casa_daste`.`articolo` (
  `codArticolo` INT NOT NULL AUTO_INCREMENT,
  `nome` VARCHAR(45) NOT NULL,
  `descrizione` TEXT(200) NOT NULL,
  `immagine` LONGBLOB NOT NULL,
  `prezzo` DECIMAL(10,2) UNSIGNED NOT NULL,
  `utente_idUtente` INT NOT NULL,
  PRIMARY KEY (`codArticolo`),
  INDEX `fk_articolo_utente1_idx` (`utente_idUtente` ASC) VISIBLE,
  CONSTRAINT `fk_articolo_utente1`
    FOREIGN KEY (`utente_idUtente`)
    REFERENCES `casa_daste`.`utente` (`idUtente`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `casa_daste`.`asta`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `casa_daste`.`asta` (
  `idAsta` INT NOT NULL AUTO_INCREMENT,
  `rialzoMin` DECIMAL(8,0) NOT NULL,
  `scadenza` DATETIME NOT NULL,
  `chiusa` BOOLEAN NOT NULL DEFAULT false,
  PRIMARY KEY (`idAsta`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `casa_daste`.`offerta`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `casa_daste`.`offerta` (
  `idOfferta` INT NOT NULL AUTO_INCREMENT,
  `prezzoOfferto` DECIMAL(10,2) NOT NULL,
  `dataOfferta` DATETIME NOT NULL,
  `utente_idUtente` INT NOT NULL,
  `asta_idAsta` INT NOT NULL,
  PRIMARY KEY (`idOfferta`),
  INDEX `fk_offerta_utente1_idx` (`utente_idUtente` ASC) VISIBLE,
  INDEX `fk_offerta_asta1_idx` (`asta_idAsta` ASC) VISIBLE,
  CONSTRAINT `fk_offerta_utente1`
    FOREIGN KEY (`utente_idUtente`)
    REFERENCES `casa_daste`.`utente` (`idUtente`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_offerta_asta1`
    FOREIGN KEY (`asta_idAsta`)
    REFERENCES `casa_daste`.`asta` (`idAsta`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `casa_daste`.`asteArticoli`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `casa_daste`.`asteArticoli` (
  `articolo_codArticolo` INT NOT NULL,
  `asta_idAsta` INT NOT NULL,
  PRIMARY KEY (`articolo_codArticolo`, `asta_idAsta`),
  INDEX `fk_asteArticoli_articolo_idx` (`articolo_codArticolo` ASC) VISIBLE,
  INDEX `fk_asteArticoli_asta1_idx` (`asta_idAsta` ASC) VISIBLE,
  CONSTRAINT `fk_asteArticoli_articolo`
    FOREIGN KEY (`articolo_codArticolo`)
    REFERENCES `casa_daste`.`articolo` (`codArticolo`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_asteArticoli_asta1`
    FOREIGN KEY (`asta_idAsta`)
    REFERENCES `casa_daste`.`asta` (`idAsta`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
