import type { Event } from '@/types'

export interface AgendaItem {
  time: string
  title: string
  description?: string
  location?: string
}

export interface AgendaDay {
  label: string
  items: AgendaItem[]
}

export interface Performer {
  id: number
  name: string
  role: string
  avatar: string
  bio: string
  headliner?: boolean
}

export interface FaqItem {
  question: string
  answer: string
}

export interface PolicyItem {
  icon: 'refund' | 'age' | 'checkin' | 'camera' | 'bag' | 'transfer'
  title: string
  content: string
}

export interface EventReview {
  id: number
  userName: string
  avatar: string
  rating: number
  comment: string
  date: string
  helpful: number
}

export interface Highlight {
  icon: 'music' | 'star' | 'users' | 'gift' | 'mic' | 'camera' | 'trophy' | 'sparkles'
  title: string
  description: string
}

export interface EventDetailExtras {
  highlights: Highlight[]
  agenda: AgendaDay[]
  performers: Performer[]
  performerLabel: string
  faqs: FaqItem[]
  policies: PolicyItem[]
  reviews: EventReview[]
  gallery: string[]
  tags: string[]
  venueInfo: {
    capacity: number
    parking: string
    transport: string
    gateOpen: string
  }
  organizer: {
    name: string
    logo: string
    verified: boolean
    eventsHosted: number
    followers: number
    description: string
  }
  interestedCount: number
}

const AVATARS = [
  'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=200&q=80',
  'https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=200&q=80',
  'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=200&q=80',
  'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=200&q=80',
  'https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=200&q=80',
  'https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?auto=format&fit=crop&w=200&q=80',
  'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=200&q=80',
  'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?auto=format&fit=crop&w=200&q=80',
]

const GALLERY: Record<string, string[]> = {
  'am-nhac': [
    'https://images.unsplash.com/photo-1501386761578-eac5c94b800a?auto=format&fit=crop&w=800&q=80',
    'https://images.unsplash.com/photo-1514525253161-7a46d19cd819?auto=format&fit=crop&w=800&q=80',
    'https://images.unsplash.com/photo-1459749411175-04bf5292ceea?auto=format&fit=crop&w=800&q=80',
    'https://images.unsplash.com/photo-1465847899084-d164df4dedc6?auto=format&fit=crop&w=800&q=80',
  ],
  'cong-nghe': [
    'https://images.unsplash.com/photo-1505373877841-8d25f7d46678?auto=format&fit=crop&w=800&q=80',
    'https://images.unsplash.com/photo-1475721027785-f74eccf877e2?auto=format&fit=crop&w=800&q=80',
    'https://images.unsplash.com/photo-1515187029135-18ee286d815b?auto=format&fit=crop&w=800&q=80',
    'https://images.unsplash.com/photo-1591115765373-5207764f72e7?auto=format&fit=crop&w=800&q=80',
  ],
  'the-thao': [
    'https://images.unsplash.com/photo-1452626038306-9aae5e071dd3?auto=format&fit=crop&w=800&q=80',
    'https://images.unsplash.com/photo-1504450758481-7338eba7524a?auto=format&fit=crop&w=800&q=80',
    'https://images.unsplash.com/photo-1517649763962-0c623066013b?auto=format&fit=crop&w=800&q=80',
    'https://images.unsplash.com/photo-1461896836934-ffe607ba8211?auto=format&fit=crop&w=800&q=80',
  ],
  default: [
    'https://images.unsplash.com/photo-1507676184212-d03ab07a01bf?auto=format&fit=crop&w=800&q=80',
    'https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?auto=format&fit=crop&w=800&q=80',
    'https://images.unsplash.com/photo-1503095396549-807759245b35?auto=format&fit=crop&w=800&q=80',
    'https://images.unsplash.com/photo-1460661419201-fd4cecdf8a8b?auto=format&fit=crop&w=800&q=80',
  ],
}

const pad = (n: number) => n.toString().padStart(2, '0')
const addMinutes = (iso: string, minutes: number) => {
  const d = new Date(iso)
  d.setMinutes(d.getMinutes() + minutes)
  return `${pad(d.getHours())}:${pad(d.getMinutes())}`
}

const COMMON_POLICIES: PolicyItem[] = [
  {
    icon: 'refund',
    title: 'Chính sách hoàn vé',
    content:
      'Hoàn 100% nếu sự kiện bị hủy hoặc dời lịch. Yêu cầu hoàn vé chủ động trước 7 ngày được hoàn 70% giá trị, sau thời điểm này vé không được hoàn.',
  },
  {
    icon: 'checkin',
    title: 'Check-in bằng QR động',
    content:
      'Mỗi vé có một mã QR duy nhất được làm mới mỗi 30 giây trong mục "Vé của tôi". Vui lòng mang theo CCCD để đối chiếu khi cần.',
  },
  {
    icon: 'transfer',
    title: 'Chuyển nhượng vé',
    content: 'Vé có thể chuyển nhượng miễn phí cho người khác qua tài khoản EventHub tối đa 1 lần, trước giờ diễn 24 giờ.',
  },
  {
    icon: 'bag',
    title: 'Vật dụng cấm mang vào',
    content: 'Không mang theo chất cấm, vũ khí, đồ uống có cồn, pháo sáng, gậy selfie và thiết bị phát sóng chuyên nghiệp.',
  },
]

function buildExtrasByCategory(event: Event) {
  const start = event.startDateTime
  const slug = event.category.slug

  if (slug === 'cong-nghe') {
    return {
      performerLabel: 'Diễn Giả Nổi Bật',
      highlights: [
        { icon: 'mic', title: '50+ diễn giả', description: 'Chuyên gia đến từ Google, AWS, Microsoft và các startup kỳ lân.' },
        { icon: 'users', title: '3.000+ người tham dự', description: 'Cơ hội networking với kỹ sư, CTO và nhà đầu tư.' },
        { icon: 'gift', title: 'Tài liệu độc quyền', description: 'Slide, video ghi hình và chứng nhận tham dự điện tử.' },
        { icon: 'sparkles', title: 'Khu trải nghiệm AI', description: 'Demo sản phẩm thực tế từ 30+ doanh nghiệp công nghệ.' },
      ] as Highlight[],
      agenda: [
        {
          label: 'Ngày 1',
          items: [
            { time: addMinutes(start, -60), title: 'Đón khách & Check-in', location: 'Sảnh chính' },
            { time: addMinutes(start, 0), title: 'Keynote: Kỷ nguyên Agentic AI', description: 'Bài phát biểu mở màn từ diễn giả chính.', location: 'Hall A' },
            { time: addMinutes(start, 90), title: 'Panel: Cloud Native & High Availability', location: 'Hall A' },
            { time: addMinutes(start, 210), title: 'Tiệc trưa & Networking', location: 'Khu ẩm thực' },
            { time: addMinutes(start, 300), title: 'Workshop chuyên sâu theo track', description: 'AI/ML • DevOps • Web3 • Product', location: 'Room 1-4' },
          ],
        },
        {
          label: 'Ngày 2',
          items: [
            { time: addMinutes(start, 0), title: 'Startup Pitching Competition', location: 'Hall A' },
            { time: addMinutes(start, 180), title: 'Fireside chat cùng nhà đầu tư', location: 'Hall B' },
            { time: addMinutes(start, 360), title: 'Lễ bế mạc & VIP Networking Dinner', location: 'Ballroom' },
          ],
        },
      ] as AgendaDay[],
      performers: [
        { id: 1, name: 'TS. David Miller', role: 'AI Research Director', avatar: AVATARS[2], bio: '15 năm nghiên cứu học máy, tác giả 40+ bài báo khoa học.', headliner: true },
        { id: 2, name: 'Nguyễn Thu Hà', role: 'CTO, FinTech Unicorn', avatar: AVATARS[1], bio: 'Dẫn dắt hệ thống thanh toán phục vụ 20 triệu người dùng.' },
        { id: 3, name: 'Trần Minh Khoa', role: 'Principal Engineer, Cloud', avatar: AVATARS[3], bio: 'Chuyên gia kiến trúc hệ thống phân tán quy mô lớn.' },
        { id: 4, name: 'Sarah Lim', role: 'Partner, Venture Capital', avatar: AVATARS[4], bio: 'Đầu tư vào 30+ startup công nghệ Đông Nam Á.' },
      ] as Performer[],
      faqs: [
        { question: 'Tôi có nhận được chứng nhận tham dự không?', answer: 'Có. Chứng nhận điện tử sẽ được gửi qua email trong vòng 3 ngày sau sự kiện.' },
        { question: 'Có cung cấp video ghi hình các phiên không?', answer: 'Người sở hữu vé VIP trở lên được truy cập toàn bộ video ghi hình trong 6 tháng.' },
      ],
      tags: ['AI', 'Cloud', 'Networking', 'Startup', 'Hội thảo'],
    }
  }

  if (slug === 'the-thao') {
    return {
      performerLabel: 'Đội Thi Đấu & Khách Mời',
      highlights: [
        { icon: 'trophy', title: 'Chuẩn quốc tế', description: 'Đường đua / sân đấu được chứng nhận tiêu chuẩn thi đấu chuyên nghiệp.' },
        { icon: 'users', title: 'Hàng nghìn VĐV & CĐV', description: 'Không khí sôi động cùng cộng đồng thể thao lớn nhất.' },
        { icon: 'gift', title: 'Race kit & quà tặng', description: 'Áo đấu, huy chương, túi quà từ nhà tài trợ.' },
        { icon: 'camera', title: 'Ảnh thi đấu miễn phí', description: 'Nhận ảnh chụp chuyên nghiệp qua số BIB / số vé.' },
      ] as Highlight[],
      agenda: [
        {
          label: 'Ngày thi đấu',
          items: [
            { time: addMinutes(start, -90), title: 'Mở cổng & Khởi động', location: 'Khu tập kết' },
            { time: addMinutes(start, -15), title: 'Lễ khai mạc', location: 'Sân khấu chính' },
            { time: addMinutes(start, 0), title: 'Bắt đầu thi đấu', description: 'Xuất phát / Tip-off chính thức.' },
            { time: addMinutes(start, 120), title: 'Biểu diễn giữa giờ', description: 'Cheerleader & DJ live set.' },
            { time: addMinutes(start, 180), title: 'Trao giải & Chụp ảnh lưu niệm', location: 'Sân khấu chính' },
          ],
        },
      ] as AgendaDay[],
      performers: [
        { id: 1, name: 'Saigon Heat', role: 'CLB chủ nhà', avatar: AVATARS[5], bio: 'Đương kim vô địch, sở hữu đội hình mạnh nhất giải.', headliner: true },
        { id: 2, name: 'Hanoi Buffaloes', role: 'CLB khách', avatar: AVATARS[3], bio: 'Đội bóng giàu truyền thống của Thủ đô.' },
        { id: 3, name: 'Lê Hoàng Nam', role: 'Đại sứ sự kiện', avatar: AVATARS[6], bio: 'VĐV marathon giữ kỷ lục quốc gia.' },
      ] as Performer[],
      faqs: [
        { question: 'Có khu vực gửi đồ không?', answer: 'Có khu gửi đồ miễn phí gần cổng chính, mở trước giờ thi đấu 2 tiếng.' },
        { question: 'Có hỗ trợ y tế tại chỗ không?', answer: 'Đội ngũ y tế và xe cứu thương túc trực trong suốt thời gian sự kiện.' },
      ],
      tags: ['Thể thao', 'Marathon', 'Ngoài trời', 'Cộng đồng'],
    }
  }

  // Music / Art / Default
  return {
    performerLabel: slug === 'am-nhac' ? 'Nghệ Sĩ Biểu Diễn' : 'Nghệ Sĩ & Ê-kíp Sáng Tạo',
    highlights: [
      { icon: 'music', title: 'Âm thanh chuẩn Hi-End', description: 'Hệ thống loa line-array và kỹ sư âm thanh quốc tế.' },
      { icon: 'sparkles', title: 'Sân khấu ánh sáng 360°', description: 'Hiệu ứng laser, LED mapping và pháo lạnh hoành tráng.' },
      { icon: 'star', title: 'Dàn nghệ sĩ đình đám', description: 'Những tên tuổi hàng đầu cùng khách mời bí mật.' },
      { icon: 'gift', title: 'Quà tặng giới hạn', description: 'Merchandise độc quyền dành cho khán giả hạng VIP.' },
    ] as Highlight[],
    agenda: [
      {
        label: 'Đêm diễn',
        items: [
          { time: addMinutes(start, -90), title: 'Mở cổng check-in', description: 'Nhận vòng tay & quà tặng tại quầy.', location: 'Cổng chính' },
          { time: addMinutes(start, -30), title: 'Khu photobooth & Merchandise', location: 'Sảnh' },
          { time: addMinutes(start, 0), title: 'Khai màn chương trình', description: 'Tiết mục mở màn đặc biệt.', location: 'Sân khấu chính' },
          { time: addMinutes(start, 75), title: 'Giải lao 15 phút', location: 'Sảnh' },
          { time: addMinutes(start, 90), title: 'Phần trình diễn chính', description: 'Những tiết mục được mong chờ nhất.', location: 'Sân khấu chính' },
          { time: addMinutes(start, 170), title: 'Encore & Bế mạc', location: 'Sân khấu chính' },
        ],
      },
    ] as AgendaDay[],
    performers: [
      { id: 1, name: 'Maestro Lê Phi', role: 'Nhạc trưởng / Giám đốc âm nhạc', avatar: AVATARS[2], bio: 'Từng chỉ huy nhiều dàn nhạc giao hưởng tại châu Âu.', headliner: true },
      { id: 2, name: 'Mỹ Tâm', role: 'Ca sĩ khách mời', avatar: AVATARS[1], bio: 'Giọng ca nữ hàng đầu với hơn 20 năm hoạt động nghệ thuật.', headliner: true },
      { id: 3, name: 'Vũ.', role: 'Ca sĩ - Nhạc sĩ', avatar: AVATARS[3], bio: 'Hoàng tử Indie với những bản ballad triệu view.' },
      { id: 4, name: 'Linh Chi', role: 'Nghệ sĩ Violin', avatar: AVATARS[7], bio: 'Thủ khoa Nhạc viện, solo cùng nhiều dàn nhạc quốc tế.' },
    ] as Performer[],
    faqs: [
      { question: 'Trẻ em có được tham dự không?', answer: 'Trẻ em từ 8 tuổi trở lên được tham dự và cần mua vé riêng; dưới 16 tuổi phải có người lớn đi kèm.' },
      { question: 'Tôi có được quay phim, chụp ảnh không?', answer: 'Được chụp ảnh bằng điện thoại không dùng flash. Nghiêm cấm thiết bị quay chuyên nghiệp.' },
    ],
    tags: ['Concert', 'Live Music', 'Giao hưởng', 'Đêm nhạc'],
  }
}

const COMMON_FAQS: FaqItem[] = [
  { question: 'Tôi sẽ nhận vé như thế nào sau khi thanh toán?', answer: 'Vé điện tử kèm mã QR được gửi ngay qua email và lưu trong mục "Vé của tôi" trên EventHub.' },
  { question: 'Ghế được giữ trong bao lâu khi tôi đặt?', answer: 'Ghế được giữ trong 15 phút để bạn hoàn tất thanh toán. Hết thời gian, ghế sẽ được mở bán lại.' },
  { question: 'Có thể xuất hóa đơn VAT không?', answer: 'Có. Vui lòng điền thông tin xuất hóa đơn tại bước thanh toán, hóa đơn điện tử được gửi trong 7 ngày làm việc.' },
  { question: 'Tôi có thể đổi hạng vé sau khi mua không?', answer: 'Bạn có thể nâng hạng vé (bù chênh lệch) trước sự kiện 48 giờ nếu hạng vé đó còn chỗ.' },
]

const REVIEWS: EventReview[] = [
  { id: 1, userName: 'Minh Hoàng', avatar: AVATARS[6], rating: 5, comment: 'Tổ chức chuyên nghiệp, check-in bằng QR rất nhanh. Âm thanh ánh sáng tuyệt vời, chắc chắn sẽ quay lại mùa sau!', date: '2025-11-22', helpful: 42 },
  { id: 2, userName: 'Trâm Anh', avatar: AVATARS[4], rating: 5, comment: 'Chọn ghế trên sơ đồ rất trực quan, ngồi đúng vị trí như hình. Nhân viên hỗ trợ nhiệt tình.', date: '2025-11-21', helpful: 31 },
  { id: 3, userName: 'Thành Long', avatar: AVATARS[5], rating: 4, comment: 'Chương trình hay, tuy nhiên khu gửi xe hơi đông lúc ra về. Nên đi sớm hoặc dùng xe công nghệ.', date: '2025-11-20', helpful: 18 },
  { id: 4, userName: 'Bảo Ngọc', avatar: AVATARS[0], rating: 5, comment: 'Đáng đồng tiền bát gạo! Vé VIP được tặng quà rất xinh, vị trí nhìn sân khấu cực rõ.', date: '2025-11-19', helpful: 12 },
]

export function getEventDetailExtras(event: Event): EventDetailExtras {
  const byCat = buildExtrasByCategory(event)
  const totalCapacity = (event.ticketTypes || []).reduce((acc, t) => acc + t.totalQuantity, 0)

  return {
    highlights: byCat.highlights,
    agenda: byCat.agenda,
    performers: byCat.performers,
    performerLabel: byCat.performerLabel,
    faqs: [...byCat.faqs, ...COMMON_FAQS],
    policies: [
      ...COMMON_POLICIES,
      {
        icon: 'age',
        title: 'Độ tuổi tham dự',
        content: 'Khán giả từ 8 tuổi trở lên. Người dưới 16 tuổi cần có phụ huynh đi kèm và mua vé riêng.',
      },
      {
        icon: 'camera',
        title: 'Hình ảnh & ghi hình',
        content: 'Sự kiện có thể được ghi hình phục vụ truyền thông. Khi tham dự, bạn đồng ý cho BTC sử dụng hình ảnh của mình.',
      },
    ],
    reviews: REVIEWS,
    gallery: GALLERY[event.category.slug] || GALLERY.default,
    tags: byCat.tags,
    venueInfo: {
      capacity: event.venue?.capacity || totalCapacity || 2000,
      parking: 'Bãi gửi xe máy & ô tô trong khuôn viên, phí 5.000đ - 30.000đ/lượt.',
      transport: 'Thuận tiện di chuyển bằng taxi, xe công nghệ hoặc xe buýt. Điểm đón trả khách tại cổng chính.',
      gateOpen: `Mở cổng lúc ${addMinutes(event.startDateTime, -90)}`,
    },
    organizer: {
      name: event.organizerName,
      logo: `https://api.dicebear.com/7.x/initials/svg?seed=${encodeURIComponent(event.organizerName)}&backgroundColor=4f46e5`,
      verified: true,
      eventsHosted: 12 + (event.id * 7) % 40,
      followers: 5200 + event.id * 1375,
      description: `${event.organizerName} là đơn vị tổ chức sự kiện uy tín, đối tác chính thức của EventHub với hàng chục chương trình quy mô lớn mỗi năm.`,
    },
    interestedCount: 1800 + event.id * 431,
  }
}
