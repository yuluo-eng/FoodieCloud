import imgDefault from '@/assets/real/drink.jpg'
import imgBeefRice from '@/assets/real/beef-rice-bowl.jpg'
import imgChicken from '@/assets/real/chicken-wings.jpg'
import imgTea from '@/assets/real/lemon-tea.jpg'
import imgNoodle from '@/assets/real/noodle.jpg'
import imgBurger from '@/assets/real/burger.jpg'
import imgRealDrink from '@/assets/real/drink.jpg'
import imgSalad from '@/assets/real/salad.jpg'

// 约定 5 类：1主食 2小吃 3饮品 4面食 5轻食
const categoryIdImageMap = {
  1: imgBeefRice,
  2: imgChicken,
  3: imgTea,
  4: imgNoodle,
  5: imgSalad,
}

const categoryNameImageMap = [
  { keys: ['主食', '米饭', '面食'], image: imgBeefRice },
  { keys: ['小吃', '炸物', '点心'], image: imgChicken },
  { keys: ['饮品', '饮料', '茶', '咖啡', '果汁'], image: imgTea },
  { keys: ['面食', '面', '拉面', '汤面', '拌面'], image: imgNoodle },
  { keys: ['轻食', '沙拉'], image: imgSalad },
]

const keywordImageMap = [
  { keys: ['牛肉饭', '牛肉', '盖饭', '饭'], image: imgBeefRice },
  { keys: ['鸡翅', '鸡排', '鸡', '炸鸡'], image: imgChicken },
  { keys: ['面', '拉面', '拌面', '汤面'], image: imgNoodle },
  { keys: ['汉堡', 'burger', '披萨', 'pizza'], image: imgBurger },
  { keys: ['沙拉', 'salad', '轻食'], image: imgSalad },
  { keys: ['红茶', '奶茶', '柠檬', '茶', '可乐', '饮料'], image: imgTea },
  { keys: ['饮品', '果汁', '咖啡'], image: imgRealDrink },
]

function fallbackImageByDish(dish) {
  const name = String(dish?.dishName || '').toLowerCase()
  const hit = keywordImageMap.find((item) =>
    item.keys.some((k) => name.includes(String(k).toLowerCase()))
  )
  if (hit) return hit.image

  const categoryName = String(dish?.categoryName || '').toLowerCase()
  const catHit = categoryNameImageMap.find((item) =>
    item.keys.some((k) => categoryName.includes(String(k).toLowerCase()))
  )
  if (catHit) return catHit.image

  return categoryIdImageMap[Number(dish?.categoryId)] || imgDefault
}

export function useDishDisplayImage() {
  function displayImage(dish) {
    const raw = String(dish?.imageUrl || '').trim()
    const isTrustedUrl =
      raw.startsWith('http://') ||
      raw.startsWith('https://') ||
      raw.startsWith('/uploads/') ||
      raw.startsWith('/dishes/') ||
      raw.startsWith('data:image')
    if (raw && isTrustedUrl) {
      return raw
    }
    return fallbackImageByDish(dish)
  }

  function onImgError(event, dish) {
    const fallback = fallbackImageByDish(dish)
    if (event.target.src !== fallback) {
      event.target.src = fallback
      return
    }
    event.target.onerror = null
    event.target.src = imgDefault
  }

  return { displayImage, onImgError, imgDefault }
}
