CREATE VIEW site_info_gnss ASSELECT i.site_name,latitude,longitude,latitude_decimal,longitude_decimal,state,country,region,monument_name,domes_number,gps,glonass,igs,	global,high_rate,hourlyFROM site_information iLEFT JOIN site_gnss gON (i.site_name=g.site_name);GRANT SELECT ON cddis.site_info_gnss TO 'ops'@'localhost';GRANT SELECT ON cddis.site_info_gnss TO 'dis'@'localhost';CREATE VIEW site_info_slr ASSELECT i.site_name,latitude,longitude,latitude_decimal,longitude_decimal,state,country,region,slr,station,domes_number,site_typeFROM site_information iLEFT JOIN site_slr sON (i.site_name=s.site_name);GRANT SELECT ON cddis.site_slr TO 'ops'@'localhost';GRANT SELECT ON cddis.site_slr TO 'dis'@'localhost';CREATE VIEW site_info_vlbi ASSELECT i.site_name,latitude,longitude,latitude_decimal,longitude_decimal,state,country,region,vlbi,ivs_code,vlbi_name,station,domes_number,site_typeFROM site_information iLEFT JOIN site_vlbi vON (i.site_name=v.site_name);GRANT SELECT ON cddis.site_vlbi TO 'ops'@'localhost';GRANT SELECT ON cddis.site_vlbi TO 'dis'@'localhost';CREATE VIEW site_info_doris ASSELECT i.site_name,latitude,longitude,latitude_decimal,longitude_decimal,state,country,region,doris,domes_numberFROM site_information iLEFT JOIN site_doris dON (i.site_name=d.site_name);GRANT SELECT ON cddis.site_info_doris TO 'ops'@'localhost';GRANT SELECT ON cddis.site_info_doris TO 'dis'@'localhost';CREATE VIEW site_info_prare ASSELECT i.site_name,latitude,longitude,latitude_decimal,longitude_decimal,state,country,region,prareFROM site_information iLEFT JOIN site_prare pON (i.site_name=p.site_name);GRANT SELECT ON cddis.site_info_prare TO 'ops'@'localhost';GRANT SELECT ON cddis.site_info_prare TO 'dis'@'localhost';CREATE VIEW site_information_full(site_name,latitude,longitude,latitude_decimal,longitude_decimal,state,country,region,gnss_name,gnss_domes,gps,glonass,igs,global,high_rate,hourly,slr_name,slr_station,slr_domes,slr_sitetype,vlbi_name,vlbi_domes,ivs_code,vlbi_fullname,vlbi_station,vlbi_sitetype,doris_name,doris_domes,prare_name) ASSELECT s1.site_name,s1.latitude,s1.longitude,s1.latitude_decimal,s1.longitude_decimal,s1.state,s1.country,s1.region,s1.monument_name,s1.domes_number,gps,glonass,igs,global,high_rate,hourly,slr,s2.station,s2.domes_number,s2.site_type,vlbi,s3.domes_number,ivs_code,vlbi_name,s3.station,s3.site_type,doris,s4.domes_number,prareFROM site_info_gnss s1,site_info_slr s2,site_info_vlbi s3,site_info_doris s4,site_info_prare s5WHERE s1.site_name=s2.site_name AND s1.site_name=s3.site_name ANDs1.site_name=s4.site_name AND s1.site_name=s5.site_name GROUP BY s1.site_name,s1.latitude,s1.longitude,s1.latitude_decimal,s1.longitude_decimal,s1.state,s1.country,s1.region,s1.monument_name,s1.domes_number,gps,glonass,igs,global,high_rate,hourly,slr,s2.station,s2.domes_number,s2.site_type,vlbi,s3.domes_number,ivs_code,vlbi_name,s3.station,s3.site_type,doris,s4.domes_number,prare;GRANT SELECT ON cddis.site_information_full TO 'ops'@'localhost';GRANT SELECT ON cddis.site_information_full TO 'dis'@'localhost';