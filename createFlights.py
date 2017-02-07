#!/usr/bin/python
# -*- coding: utf-8 -*-

import sys
import random

if __name__ == '__main__' :

	city = ["Madrid","Barcelona","Valencia","Sevilla","Bilbao","Zaragoza","Lleida","Tenerife",
			"Alicante","Mallorca","A Coruña","Oviedo","Logroño","Málaga","Granada","Tarragona",
			"Girona","Huesca","Teruel","Castellón","Cáceres","Badajoz","Vitoria","San Sebastián",
			"Santander", "Pamplona", "Murcia"]
	num_cities = len(city)
	price = 0
	src = 0
	dest = 0

	ff = open('flights.csv', 'w+')
	for i in xrange(int(sys.argv[1])):
		price = random.randint(50, 1000)
		src = random.randint(0,len(city)-1)
		dest = random.randint(0,len(city)-1)
		ff.write(city[src]+","+city[dest]+","+str(i)+","+str(price)+"\n")
