import zipfile,struct,sys,hashlib,argparse
from functools import cached_property
class Dex:
 def __init__(self, data, name):
  self.b=data;self.name=name
 def u4(self,o):return struct.unpack_from('<I',self.b,o)[0]
 def u2(self,o):return struct.unpack_from('<H',self.b,o)[0]
 def uleb(self,o):
  v=0;n=0
  while True:
   b=self.b[o];o+=1;v|=(b&0x7f)<<(7*n);n+=1
   if not b&0x80 or n>=5:break
  return v,o
 @cached_property
 def strings(self):
  size,off=self.u4(56),self.u4(60)
  out=[]
  for j in range(size):
   at=self.u4(off+4*j)
   _,at=self.uleb(at)
   end=self.b.find(b'\x00',at)
   out.append(self.b[at:end].decode('utf-8','replace') if end!=-1 else '')
  return out
 @cached_property
 def types(self):
  n,off=self.u4(64),self.u4(68)
  s=self.strings
  return [s[self.u4(off+4*i)] for i in range(n)]
 @cached_property
 def fields(self):
  n,off=self.u4(80),self.u4(84)
  t=self.types;s=self.strings
  return [(t[self.u2(off+i*8)],t[self.u2(off+i*8+2)],s[self.u4(off+i*8+4)]) for i in range(n)]
 @cached_property
 def methods(self):
  n,off=self.u4(88),self.u4(92)
  t=self.types;s=self.strings
  result=[]
  for i in range(n):
   cl,pr,nm=struct.unpack_from('<HHI',self.b,off+i*8)
   result.append((t[cl],s[nm],pr))
  return result
 @cached_property
 def protos(self):
  n,off=self.u4(72),self.u4(76);t=self.types
  result=[]
  for i in range(n):
   shorty,ret,pars=struct.unpack_from('<III',self.b,off+12*i)
   params=[]
   if pars:
    size=self.u4(pars)
    if 0<size<=100:
     params=[t[self.u2(pars+4+2*j)] for j in range(size)]
   result.append((''.join(params),t[ret]))
  return result
 @cached_property
 def definitions(self):
  n,off=self.u4(96),self.u4(100);out={}
  for i in range(n):
   ci,flags,superidx,interface_off,source_idx,ann_off,classdata,static_values=struct.unpack_from('<8I',self.b,off+32*i)
   out[self.types[ci]]=classdata
  return out
 def codeoffsets(self,klass):
  off=self.definitions.get(klass,0)
  if not off:return {}
  counts=[]
  for _ in range(4):
   v,off=self.uleb(off);counts.append(v)
  si,ii,dm,vm=counts
  for _ in range(si+ii):
   _,off=self.uleb(off);_,off=self.uleb(off)
  result={}
  for size in (dm,vm):
   idx=0
   for _ in range(size):
    diff,off=self.uleb(off);idx+=diff
    _,off=self.uleb(off);pos,off=self.uleb(off)
    if pos: result[idx]=pos
  return result
 def code(self,offset):
  if not offset:return []
  size=self.u4(offset+12)
  return list(struct.unpack_from('<'+str(size)+'H',self.b,offset+16))
 def method_string(self,idx):
  k,n,p=self.methods[idx]; args,ret=self.protos[p]
  return k+'->'+n+'('+args+')'+ret
 def find(self,cl,name):
  found=[]
  for mid,off in self.codeoffsets(cl).items():
   if self.methods[mid][1] == name: found.append((mid,self.method_string(mid),off))
  return found

def width(c,i):
 op=c[i]&255
 if op==0:
  k=c[i]>>8
  if k==1 and i+2<len(c):return 4+2*c[i+1]
  if k==2 and i+2<len(c):return 2+4*c[i+1]
  if k==3 and i+3<len(c):return 4+(c[i+1]*(c[i+2]|(c[i+3]<<16))+1)//2
  return 1
 if op in (0x02,0x05,0x08,0x13,0x15,0x16,0x19,0x1a,0x1c,0x1f,0x20,0x22,0x23,0x29) or 0x2d<=op<=0x3d or 0x44<=op<=0x6d or 0xd0<=op<=0xe2 or op in (0xfe,0xff):return 2
 if op in (0x03,0x06,0x09,0x14,0x17,0x1b,0x24,0x25,0x26,0x2a,0x2b,0x2c) or 0x6e<=op<=0x72 or 0x74<=op<=0x78 or op in (0xfc,0xfd):return 3
 if op==0x18:return 5
 if op in (0xfa,0xfb):return 4
 # 0x90..0xaf should be 2
 if 0x90<=op<=0xaf:return 2
 return 1

def inspect(d,code,showall=False):
 out=[];i=0
 while i<len(code):
  op=code[i]&255;w=width(code,i)
  if w<1 or i+w>len(code):break
  if op in (0x1a,0x1b):
   idx=code[i+1] if op==0x1a else code[i+1]|(code[i+2]<<16)
   if idx<len(d.strings):out.append((i,'STRING',repr(d.strings[idx])[:180]))
  if 0x52<=op<=0x6d:
   idx=code[i+1]
   if idx<len(d.fields):out.append((i,'FIELD',str(d.fields[idx])))
  if 0x6e<=op<=0x72 or 0x74<=op<=0x78 or op in (0xfa,0xfb,0xfc,0xfd):
   idx=code[i+1]
   if idx<len(d.methods):out.append((i,'INVOKE',d.method_string(idx)))
  if showall:out.append((i,'OP',f'0x{op:02x} '+' '.join(f'{x:04x}' for x in code[i:i+w])))
  i+=w
 return out


EXPECTED_IG_SHA="fb6ca8c21d909d95892e67339746b8fe43f409a9dea11f87d9ab0cee8e97c5b6"
EXPECTED_IE_SHA="b1308bdf4edc2956671fe5a0ec04972ba77a25edd51e144072b9b157755d2dc5"
EXPECTED_IE_REV="ee1c81cadfbcd9fbbee4d01568ee5ca4b2653fbc"

def file_hash(path):
    h=hashlib.sha256()
    with open(path,"rb") as stream:
        for part in iter(lambda:stream.read(1048576),b""):h.update(part)
    return h.hexdigest()

def check(instagram_apk,instaeclipse_apk):
    from pathlib import Path
    if Path(instagram_apk).stat().st_size!=238346353 or file_hash(instagram_apk)!=EXPECTED_IG_SHA:
        raise RuntimeError("REFUSED: Instagram 455 exact APK bytes/hash mismatch")
    if Path(instaeclipse_apk).stat().st_size!=15621572 or file_hash(instaeclipse_apk)!=EXPECTED_IE_SHA:
        raise RuntimeError("REFUSED: InstaEclipse 0.8.2 exact APK bytes/hash mismatch")
    with zipfile.ZipFile(instaeclipse_apk) as z:
        assert z.testzip() is None, "IE APK archive CRC"
        assert z.read('META-INF/xposed/scope.list').decode().strip()=='com.instagram.android', "scope mismatch"
        assert EXPECTED_IE_REV.encode() in z.read('META-INF/version-control-info.textproto'), "IE source rev mismatch"
        assert b'targetApiVersion=102' in z.read('META-INF/xposed/module.prop'), "API102 mismatch"
    with zipfile.ZipFile(instagram_apk) as z:
        assert z.testzip() is None, "IG APK archive CRC"
        assert len([p for p in z.namelist() if p.startswith('classes') and p.endswith('.dex')])==31, "DEX roster"
        d=Dex(z.read('classes5.dex'),'classes5.dex')
        methods=d.find('LX/07EF;','A05')
        assert len(methods)==1, "unique candidate method"
        method=methods[0][1]
        assert method=='LX/07EF;->A05(LX/03jS;Lcom/instagram/feed/media/Media;)Ljava/util/ArrayList;', "signature"
        refs=inspect(d,d.code(methods[0][2]))
        fields=[(x[2]) for x in refs if x[1]=='FIELD']
        for f in ('PLAYBACK_CONTROLS','UNSAVE'):
            assert any('MediaOption$Option' in x and "'"+f+"'" in x for x in fields), f+" field-use absent"
        assert any("'LX/07EF;'" in x and "'A08'" in x and 'ClipsViewerConfig' in x for x in fields), "builder config field absent"
        assert any('ClipsViewerConfig' in x and "'A00'" in x and 'ClipsViewerSource' in x for x in fields), "viewer source field absent"
        e=Dex(z.read('classes4.dex'),'classes4.dex')
        enum=e.find('Lcom/instagram/clips/intf/ClipsViewerSource;','<clinit>')
        assert len(enum)==1, "source enum missing"
        strings={x[2].strip("'") for x in inspect(e,e.code(enum[0][2])) if x[1]=='STRING'}
        for word in ('CLIPS_TAB','CLIPS_TAB_HOMECOMING','FEED_TIMELINE','REEL_FEED_TIMELINE'):
            assert word in strings, word+' enum absent'
    return 'INA_D14_EXACT_INSTA_ECLIPSE_APK_AND_IG455_DEX_FIELD_USE_PASS'

if __name__=='__main__':
    p=argparse.ArgumentParser(description='Static exact APK provenance; no device or network access')
    p.add_argument('--instagram-apk',required=True)
    p.add_argument('--instaeclipse-apk',required=True)
    a=p.parse_args()
    try:print(check(a.instagram_apk,a.instaeclipse_apk))
    except Exception as e:
        print('INA_D14_STATIC_APK_REFUSED: '+str(e),file=sys.stderr)
        raise SystemExit(2)
