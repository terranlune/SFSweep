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

    self.data = {"kml_id" : "", "name" : ""}
    for k in self.fields:
      self.data[k] = ""

    self.inField = None
    self.coordinatesBuffer = None


  def startElement(self, name, attributes):

    if name == "Placemark":
      self.kml_id = attributes["id"]

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

      self.coordinatesBuffer = self.coordinatesBuffer.strip()
      raw_coordinates = self.coordinatesBuffer.split(" ")

      coordinates = []
      for raw_coordinate in self.coordinatesBuffer.split(" "):
        x = raw_coordinate.split(",")

        # Note we're reordering to have lat before lng
        coordinates.append((float(x[1]), float(x[0])))

      maxLatLng = map(max, zip(*coordinates))
      minLatLng = map(min, zip(*coordinates))

      self.data["min_latitude"] = minLatLng[0]
      self.data["max_latitude"] = maxLatLng[0]
      self.data["min_longitude"] = minLatLng[1]
      self.data["max_longitude"] = maxLatLng[1]

      self.data["coordinates"] = " ".join(["%s,%s" % (i[0], i[1]) for i in coordinates])

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

    # Android metadata
    c.execute("""DROP TABLE IF EXISTS android_metadata""")
    c.execute("""CREATE TABLE "android_metadata" ("locale" TEXT DEFAULT 'en_US')""")
    c.execute("""INSERT INTO "android_metadata" VALUES ('en_US')""")

    if not append:
        c.execute("""DROP TABLE IF EXISTS street_sweeper_data""")
    c.execute("""CREATE TABLE street_sweeper_data (
              Id integer PRIMARY KEY AUTOINCREMENT,
              kml_id text,
              name text, 
              coordinates text,
              min_latitude numeric, 
              min_longitude numeric, 
              max_latitude numeric, 
              max_longitude numeric, %s text)""" % " text, ".join(StreetSweepHandler.fields))
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
