import struct,zipfile

def uleb(d,off):
 n=0;s=0
 for i in range(5):
  b=d[off+i];n|=(b&127)<<s;s+=7
  if b<128:return n,off+i+1
 raise ValueError('uleb')

def widths(op, hi, seq, i):
 if op==0:
  if hi==1:return 4+2*seq[i+1] if i+1<len(seq) else 1
  if hi==2:return 2+4*seq[i+1] if i+1<len(seq) else 1
  if hi==3:return 4+((seq[i+1]*(seq[i+2] | seq[i+3]<<16))+1)//2 if i+3<len(seq) else 1
  return 1
 if op in (0x02,0x05,0x08,0x13,0x15,0x16,0x19,0x1a,0x1c,0x1f,0x20,0x22,0x23,0x29):return 2
 if op in (0x03,0x06,0x09,0x14,0x17,0x1b,0x24,0x25,0x26,0x2a,0x2b,0x2c):return 3
 if op==0x18:return 5
 if op in (0x21,0x28,0x01,0x04,0x07,0x0a,0x0b,0x0c,0x0d,0x0e,0x0f,0x10,0x11,0x12,0x1d,0x1e,0x27):return 1
 if 0x2d<=op<=0x3d:return 2
 if 0x44<=op<=0x6d:return 2
 if 0x6e<=op<=0x72 or 0x74<=op<=0x78:return 3
 if 0x7b<=op<=0x8f:return 1
 if 0x90<=op<=0xaf:return 2
 if 0xb0<=op<=0xcf:return 1
 if 0xd0<=op<=0xe2:return 2
 if op in (0xfa,0xfb):return 4
 if op in (0xfc,0xfd):return 3
 if op in (0xfe,0xff):return 2
 if op==0x73 or op==0x79 or op==0x7a:return 1
 return 1

class Dex:
 def __init__(self,d,filename):
  self.d=d;self.filename=filename
  def U(off):return struct.unpack_from('<I',d,off)[0]
  self.num_str,self.str_off=U(0x38),U(0x3c)
  self.num_types,self.type_off=U(0x40),U(0x44)
  self.num_proto,self.proto_off=U(0x48),U(0x4c)
  self.num_method,self.method_off=U(0x58),U(0x5c)
  self.num_class,self.class_off=U(0x60),U(0x64)
  self.strings=[]
  for i in range(self.num_str):
   off=U(self.str_off+4*i)
   _,off=uleb(d,off)
   end=d.find(b'\x00',off)
   self.strings.append(d[off:end].decode('utf-8','replace'))
  self.types=[self.strings[U(self.type_off+4*i)] for i in range(self.num_types)]
  self.protos=[]
  for i in range(self.num_proto):
   off=self.proto_off+12*i;ret=U(off+4);arg_off=U(off+8)
   ps=tuple(self.types[struct.unpack_from('<H',d,arg_off+4+2*j)[0]] for j in range(U(arg_off))) if arg_off else ()
   self.protos.append((self.types[ret],ps))
  self.methods=[]
  for i in range(self.num_method):
   class_idx,proto_idx,name_idx=struct.unpack_from('<HHI',d,self.method_off+8*i)
   self.methods.append((self.types[class_idx],self.strings[name_idx],self.protos[proto_idx]))
 def enum_code(self):
  d=self.d
  for i in range(self.num_class):
   off=self.class_off+32*i
   data_off=struct.unpack_from('<I',d,off+24)[0]
   if not data_off:continue
   sizes=[]
   for j in range(4):
    n,data_off=uleb(d,data_off);sizes.append(n)
   for count in sizes[:2]:
    for _ in range(count):
     _,data_off=uleb(d,data_off);_,data_off=uleb(d,data_off)
   for count in sizes[2:]:
    midx=0
    for _ in range(count):
     delta,data_off=uleb(d,data_off);midx+=delta
     access,data_off=uleb(d,data_off)
     code_off,data_off=uleb(d,data_off)
     if code_off:
      size=struct.unpack_from('<I',d,code_off+12)[0]
      if size>120000:continue
      code=struct.unpack_from('<'+str(size)+'H',d,code_off+16)
      yield midx,code

def find(apk_path):
 import argparse,hashlib,json
 EXPECTED_HASH="4f5bc1bd5e7b2be6e0634200d01926ddea1096c6fc780312fe0452ef66d28c0a"
 EXPECTED={
  ("classes.dex","LX/06nv;","A03",6138),
  ("classes6.dex","LX/043F;","A00",1615),
 }
 digest=hashlib.sha256()
 with open(apk_path,"rb") as f:
  while True:
   b=f.read(1024*1024)
   if not b:break
   digest.update(b)
 actual=digest.hexdigest()
 if actual!=EXPECTED_HASH:raise SystemExit("REFUSED: unsupported Instagram base.apk SHA256 "+actual)
 target="Lcom/instagram/feed/media/Media;";t="AFV"
 found=[]
 with zipfile.ZipFile(apk_path) as z:
  dexes=sorted((x for x in z.namelist() if x.startswith("classes") and x.endswith(".dex")),
      key=lambda n:int(n[7:-4]) if n!="classes.dex" else 1)
  if len(dexes)!=31:raise SystemExit("REFUSED: unexpected DEX inventory")
  for name in dexes:
   dex=Dex(z.read(name),name)
   targetidx={i for i,m in enumerate(dex.methods)
       if m[0]==target and m[1]==t and m[2]==("V",("Ljava/lang/Boolean;",))}
   if not targetidx:continue
   for caller,code in dex.enum_code():
    i=0
    while i<len(code):
     op=code[i]&255;hi=code[i]>>8
     width=widths(op,hi,code,i)
     if width<=0:raise SystemExit("REFUSED: decoder unsupported opcode")
     if op in (0x6e,0x6f,0x70,0x71,0x72,0x74,0x75,0x76,0x77,0x78) and i+1<len(code):
      mid=code[i+1]
      if mid in targetidx:
       meth=dex.methods[caller]
       found.append((name,meth[0],meth[1],i))
     i+=width
 if set(found)!=EXPECTED or len(found)!=len(EXPECTED):
  raise SystemExit("REFUSED: AFV direct invoke inventory drift "+str(found))
 print(json.dumps({"status":"RCA02_IG455_AFV_DIRECT_CALLSITES_PASS",
   "base_sha256":actual,"dex_count":31,"callers":[
   {"dex":row[0],"class":row[1],"method":row[2],"dex_pc":row[3]}
    for row in sorted(found)],
   "limitations":["direct invoke only","server value not established",
                  "GraphQL-to-Media binding not established"]},indent=2))

if __name__=='__main__':
 import argparse
 p=argparse.ArgumentParser(description="Offline, read-only SHA-pinned IG455 AFV caller audit")
 p.add_argument('--base-apk',required=True)
 args=p.parse_args()
 find(args.base_apk)
