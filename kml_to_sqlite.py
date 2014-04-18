import xml.sax
import xml.sax.handler
import pprint
import sqlite3
from optparse import OptionParser

class StreetSweepHandler(xml.sax.handler.ContentHandler):

  fields = [
    "CNN", "WeekDay", "BlockSide", "BlockSweepID", "CNNRightLeft", 
     "Corridor", "FromHour", "ToHour", "Holidays", "Week1OfMonth", 
    "Week2OfMonth", "Week3OfMonth", "Week4OfMonth", "Week5OfMonth", 
    "LF_FADD", "LF_TOADD", "RT_TOADD", "RT_FADD", "STREETNAME", 
    "ZIP_CODE", "NHOOD", "DISTRICT"
  ]

  def __init__(self, cursor):
    self.reset()
    self.cursor = cursor
    self.count = 0;
 
  def reset(self):

    self.data = {"id" : "", "name" : ""}
    for k in self.fields:
      self.data[k] = ""

    self.inField = None
    self.coordinatesBuffer = None


  def startElement(self, name, attributes):

    if name == "Placemark":
      self.id = attributes["id"]

    elif name == "name":
      self.inField = "name"

    elif name == "SimpleData":
      self.inField = attributes["name"]

    elif name == "coordinates":
      self.coordinatesBuffer = ""
 
  def characters(self, data):
    if self.inField:
      self.data[self.inField] += data

    elif self.coordinatesBuffer is not None:
      self.coordinatesBuffer += data
 
  def endElement(self, name):
    if name == "SimpleData":
      self.inField = None

    elif name == "name":
      self.inField = None

    elif name == "coordinates":
      coordinates = self.coordinatesBuffer.split(" ")

      begin = coordinates[0].split(",")
      self.data["latitude"] = begin[1]
      self.data["longitude"] = begin[0]

      end = coordinates[1].split(",")
      self.data["end_latitude"] = end[1]
      self.data["end_longitude"] = end[0]

      self.coordinatesBuffer = None

    elif name == "Placemark":
      statement = """INSERT INTO street_sweeper_data
        ('%s') VALUES (%s)""" % (
                               "','".join(self.data.keys()), 
                               ",".join("?"*len(self.data.values()))
                              )
      self.cursor.execute(statement, self.data.values())
      self.reset()
      self.count += 1

def convertData(fromKml, toSqlite, append):
    conn = sqlite3.connect(toSqlite)
    c = conn.cursor()
    if not append:
        c.execute("""DROP TABLE IF EXISTS street_sweeper_data""")
    c.execute("""CREATE TABLE street_sweeper_data (
              id text,
              name text, 
              latitude numeric, 
              longitude numeric, 
              end_latitude numeric, 
              end_longitude numeric, %s text)""" % " text, ".join(StreetSweepHandler.fields))
    parser = xml.sax.make_parser(  )
    handler = StreetSweepHandler(c)
    parser.setContentHandler(handler)
    parser.parse(fromKml)
    conn.commit()
    print "Inserted %s records" % handler.count

parser = OptionParser()
parser.add_option("-i", "--input", help="StreetSweeper .kml file")
parser.add_option("-o", "--output", help="SQLite3 .db file")
parser.add_option("-a", "--append", action="store_true", default=False, help="Append instead of replacing db contents")
(options, args) = parser.parse_args()

convertData(options.input, options.output, options.append)
