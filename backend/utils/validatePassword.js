// opcijsko je to za validiranje passworda (login, register), drugace so se druge funkcije v JS ki to samo preverjajo, ce je match
// bcrypt, 
/*
async function hashPassword(password) {
  const salt = await bcrypt.genSalt(10)
  const hash = await bcrypt.hash(password, salt)
  return hash
}
const hashedPassword = await hashPassword('mojegeslo')


async function checkPassword(enteredPass, hashedPasswordFromDB) {
  return await bcrypt.compare(enteredPass, hashedPasswordFromDB) // true ali false vrne
}

*/