from org.lcsim.util import Driver

class VtxTest(Driver):
	def process(self, event):
		tracks = event.getTracks