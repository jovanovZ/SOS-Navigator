
const jwt = require('jsonwebtoken');
const JWT_SECRET = process.env.JWT_SECRET || 'my_secret';

const authMiddleware = (req, res, next) => {
  try {
    const token = req.cookies.token;
    if (!token) return res.status(401).json({ message: 'Not authenticated' });

    const decoded = jwt.verify(token, JWT_SECRET);
    req.user = { _id: decoded._id };    
    next();
  } catch (err) {
    return res.status(401).json({ message: 'Invalid or expired token' });
  }
};

module.exports = authMiddleware;